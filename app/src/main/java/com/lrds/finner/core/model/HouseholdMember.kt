package com.lrds.finner.core.model

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class HouseholdSession(
    val userId: String,
    val userDisplayName: String,
    val userEmail: String? = null,
    val householdId: String,
    val householdName: String,
    val isFirebaseAuthenticated: Boolean = false
)

class HouseholdAuthManager(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(loadInitialSession())
    val session: StateFlow<HouseholdSession> = _session.asStateFlow()

    private val firebaseAuthOrNull: FirebaseAuth?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not initialized yet; using local profile session.", e)
            null
        }

    private val firestoreOrNull: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

    private fun loadInitialSession(): HouseholdSession {
        val authUser = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

        val savedLocalUserId = prefs.getString(KEY_USER_ID, null) ?: run {
            val generated = "user_${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString(KEY_USER_ID, generated).apply()
            generated
        }

        val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
            ?: authUser?.displayName?.takeIf { it.isNotBlank() }
            ?: "Member 1"
        val savedHouseholdId = prefs.getString(KEY_HOUSEHOLD_ID, DEFAULT_HOUSEHOLD_ID) ?: DEFAULT_HOUSEHOLD_ID
        val savedHouseholdName = prefs.getString(KEY_HOUSEHOLD_NAME, DEFAULT_HOUSEHOLD_NAME) ?: DEFAULT_HOUSEHOLD_NAME

        return HouseholdSession(
            userId = authUser?.uid ?: savedLocalUserId,
            userDisplayName = savedDisplayName,
            userEmail = authUser?.email,
            householdId = savedHouseholdId,
            householdName = savedHouseholdName,
            isFirebaseAuthenticated = authUser != null
        )
    }

    fun getConfiguredWebClientId(): String? {
        val resId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (resId != 0) {
            val fromRes = context.getString(resId).trim()
            if (fromRes.isNotEmpty()) return fromRes
        }
        return prefs.getString(KEY_WEB_CLIENT_ID, null)?.takeIf { it.isNotBlank() }
    }

    fun updateProfileAndWorkspace(
        displayName: String,
        householdId: String,
        householdName: String
    ) {
        val cleanName = displayName.trim().ifEmpty { "Member" }
        val cleanHouseholdId = householdId.trim().lowercase().replace(" ", "_").ifEmpty { DEFAULT_HOUSEHOLD_ID }
        val cleanHouseholdName = householdName.trim().ifEmpty { DEFAULT_HOUSEHOLD_NAME }

        prefs.edit()
            .putString(KEY_DISPLAY_NAME, cleanName)
            .putString(KEY_HOUSEHOLD_ID, cleanHouseholdId)
            .putString(KEY_HOUSEHOLD_NAME, cleanHouseholdName)
            .apply()

        _session.value = _session.value.copy(
            userDisplayName = cleanName,
            householdId = cleanHouseholdId,
            householdName = cleanHouseholdName
        )
    }

    suspend fun signInOrRegisterWithEmail(
        email: String,
        password: String,
        displayName: String,
        householdId: String
    ): Result<HouseholdSession> {
        val auth = firebaseAuthOrNull
            ?: return Result.failure(IllegalStateException("Firebase Auth not initialized."))

        return try {
            val authResult = try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            } catch (_: Exception) {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            }
            val user = authResult.user
            val uid = user?.uid ?: _session.value.userId
            val resolvedName = displayName.trim().ifEmpty {
                user?.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
            }

            updateProfileAndWorkspace(
                displayName = resolvedName,
                householdId = householdId,
                householdName = _session.value.householdName
            )
            ensureHouseholdMembershipInFirestore(
                uid = uid,
                householdId = _session.value.householdId,
                householdName = _session.value.householdName
            )

            val updated = _session.value.copy(
                userId = uid,
                userDisplayName = resolvedName,
                userEmail = user?.email ?: email.trim(),
                isFirebaseAuthenticated = true
            )
            _session.value = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(
        activityContext: Context,
        customWebClientId: String?,
        displayName: String,
        householdId: String
    ): Result<HouseholdSession> {
        val auth = firebaseAuthOrNull
            ?: return Result.failure(IllegalStateException("Firebase Auth not initialized."))

        val resolvedWebClientId = customWebClientId?.trim()?.takeIf { it.isNotEmpty() }
            ?: getConfiguredWebClientId()
            ?: return Result.failure(
                IllegalStateException(
                    "Enable the Google provider in Firebase Console (Authentication > Sign-in method) and enter the Web Client ID or update google-services.json."
                )
            )

        prefs.edit().putString(KEY_WEB_CLIENT_ID, resolvedWebClientId).apply()

        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(resolvedWebClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = credentialResponse.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                val uid = user?.uid ?: _session.value.userId
                val resolvedName = user?.displayName?.takeIf { it.isNotBlank() }
                    ?: googleIdTokenCredential.displayName?.takeIf { it.isNotBlank() }
                    ?: displayName.trim().ifEmpty { "Member" }

                updateProfileAndWorkspace(
                    displayName = resolvedName,
                    householdId = householdId,
                    householdName = _session.value.householdName
                )
                ensureHouseholdMembershipInFirestore(
                    uid = uid,
                    householdId = _session.value.householdId,
                    householdName = _session.value.householdName
                )

                val updated = _session.value.copy(
                    userId = uid,
                    userDisplayName = resolvedName,
                    userEmail = user?.email,
                    isFirebaseAuthenticated = true
                )
                _session.value = updated
                Result.success(updated)
            } else {
                Result.failure(IllegalStateException("Invalid Google credential returned by the system."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureHouseholdMembershipInFirestore(
        uid: String,
        householdId: String,
        householdName: String
    ) {
        val db = firestoreOrNull ?: return
        try {
            val docRef = db.collection("households").document(householdId)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val newHousehold = mapOf(
                    "name" to householdName,
                    "ownerUid" to uid,
                    "memberUids" to listOf(uid),
                    "updatedAtEpochMillis" to System.currentTimeMillis()
                )
                docRef.set(newHousehold).await()
            } else {
                docRef.update(
                    mapOf(
                        "memberUids" to FieldValue.arrayUnion(uid),
                        "updatedAtEpochMillis" to System.currentTimeMillis()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Deferred household document registration for $householdId", e)
        }
    }

    fun signOut() {
        firebaseAuthOrNull?.signOut()
        _session.value = loadInitialSession().copy(isFirebaseAuthenticated = false, userEmail = null)
    }

    fun loadAvailableYears(): List<Int> {
        val currentYear = LocalDate.now().year
        val raw = prefs.getString(KEY_AVAILABLE_YEARS, null)
        if (raw.isNullOrBlank()) {
            return listOf(currentYear)
        }
        val parsed = raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .distinct()
            .sorted()
        return if (parsed.isEmpty()) listOf(currentYear) else parsed
    }

    fun saveAvailableYears(years: List<Int>) {
        val clean = years.distinct().sorted()
        prefs.edit().putString(KEY_AVAILABLE_YEARS, clean.joinToString(",")).apply()
    }

    /**
     * Returns the first active month (1..12) for the given [year]:
     * - For the initial year when the app starts from scratch, returns the month when the app
     *   was first initialized (current calendar month onward, e.g., November..December).
     * - For any subsequent years added by the user, returns 1 (full 12 months: January..December).
     */
    fun getStartMonthForYear(year: Int): Int {
        val now = LocalDate.now()
        val initialYear = prefs.getInt(KEY_INITIAL_YEAR, -1).let { saved ->
            if (saved == -1) {
                prefs.edit()
                    .putInt(KEY_INITIAL_YEAR, now.year)
                    .putInt(KEY_INITIAL_START_MONTH, now.monthValue)
                    .apply()
                now.year
            } else {
                saved
            }
        }
        val initialStartMonth = prefs.getInt(KEY_INITIAL_START_MONTH, now.monthValue).coerceIn(1, 12)
        return if (year == initialYear) initialStartMonth else 1
    }

    companion object {
        private const val TAG = "HouseholdAuthManager"
        private const val PREFS_NAME = "finner_household_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_HOUSEHOLD_ID = "household_id"
        private const val KEY_HOUSEHOLD_NAME = "household_name"
        private const val KEY_WEB_CLIENT_ID = "web_client_id"
        private const val KEY_AVAILABLE_YEARS = "available_years"
        private const val KEY_INITIAL_YEAR = "initial_year"
        private const val KEY_INITIAL_START_MONTH = "initial_start_month"
        const val DEFAULT_HOUSEHOLD_ID = "family_2026"
        const val DEFAULT_HOUSEHOLD_NAME = "Family Budget"
    }
}
