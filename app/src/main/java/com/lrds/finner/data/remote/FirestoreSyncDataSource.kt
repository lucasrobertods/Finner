package com.lrds.finner.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.lrds.finner.core.model.HouseholdAuthManager
import com.lrds.finner.data.local.FinnerDatabase
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.EnvelopeType
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.GoalPriority
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.SyncStatus
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class CloudSyncState {
    OFFLINE_LOCAL_ONLY,
    CLOUD_CONNECTED
}

class FirestoreSyncDataSource(
    private val context: Context,
    private val authManager: HouseholdAuthManager
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeListeners: List<ListenerRegistration> = emptyList()
    private var listeningHouseholdId: String? = null

    private val firestoreOrNull: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not configured yet; operating in offline-first Room mode.", e)
            null
        }

    private val currentHouseholdId: String
        get() = authManager.session.value.householdId

    val syncState: CloudSyncState
        get() = if (firestoreOrNull != null) CloudSyncState.CLOUD_CONNECTED else CloudSyncState.OFFLINE_LOCAL_ONLY

    /**
     * Starts real-time Firestore snapshot listeners for the active household workspace
     * so multiple devices receive live updates into the local Room cache.
     */
    fun attachRealtimeListeners(database: FinnerDatabase, householdId: String = currentHouseholdId) {
        val db = firestoreOrNull ?: return
        if (listeningHouseholdId == householdId && activeListeners.isNotEmpty()) return

        activeListeners.forEach { it.remove() }
        listeningHouseholdId = householdId

        val householdDoc = db.collection("households").document(householdId)

        val categoryListener = householdDoc.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val now = LocalDate.now()
                val remoteCategories = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE_LOCAL")
                        val rawEnvelopeName = doc.getString("envelopeName")
                            ?: doc.getString("envelopeType")?.let { rawEnum ->
                                runCatching { EnvelopeType.valueOf(rawEnum).displayName }.getOrDefault(rawEnum)
                            }
                        val resolvedEnvelope = if (type == TransactionType.INCOME) {
                            null
                        } else {
                            rawEnvelopeName?.takeIf { it.isNotBlank() } ?: EnvelopeType.BILLS.displayName
                        }
                        CategoryEntity(
                            id = id,
                            seriesId = doc.getString("seriesId") ?: id,
                            year = (doc.getLong("year") ?: now.year.toLong()).toInt(),
                            month = (doc.getLong("month") ?: now.monthValue.toLong()).toInt(),
                            name = doc.getString("name") ?: "",
                            type = type,
                            envelopeName = resolvedEnvelope,
                            plannedAmountCents = doc.getLong("plannedAmountCents") ?: 0L,
                            plannedPercentage = doc.getDouble("plannedPercentage"),
                            currency = CurrencyCode.valueOf(doc.getString("currency") ?: "EUR"),
                            iconKey = doc.getString("iconKey") ?: "category",
                            colorHex = doc.getLong("colorHex") ?: 0xFF2E7D32L,
                            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            isDeleted = doc.getBoolean("isDeleted") ?: false,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteCategories.isNotEmpty()) {
                    syncScope.launch {
                        database.categoryDao().upsertAll(remoteCategories)
                    }
                }
            }

        val bucketListener = householdDoc.collection("budget_buckets")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val remoteBuckets = snapshot.documents.mapNotNull { doc ->
                    try {
                        val name = doc.getString("bucketName")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        BudgetBucketEntity(
                            bucketName = name,
                            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            isDeleted = doc.getBoolean("isDeleted") ?: false,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteBuckets.isNotEmpty()) {
                    syncScope.launch {
                        database.budgetBucketDao().upsertAll(remoteBuckets)
                    }
                }
            }

        val txListener = householdDoc.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val remoteTransactions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dateStr = doc.getString("date") ?: return@mapNotNull null
                        val date = LocalDate.parse(dateStr)
                        TransactionEntity(
                            id = doc.getString("id") ?: doc.id,
                            description = doc.getString("description") ?: "",
                            amountCents = doc.getLong("amountCents") ?: 0L,
                            currency = CurrencyCode.valueOf(doc.getString("currency") ?: "EUR"),
                            date = date,
                            year = (doc.getLong("year") ?: date.year.toLong()).toInt(),
                            month = (doc.getLong("month") ?: date.monthValue.toLong()).toInt(),
                            categoryId = doc.getString("categoryId"),
                            type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE_LOCAL"),
                            userId = doc.getString("userId") ?: "shared",
                            userDisplayName = doc.getString("userDisplayName") ?: "",
                            isPaid = doc.getBoolean("isPaid") ?: true,
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            isDeleted = doc.getBoolean("isDeleted") ?: false,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteTransactions.isNotEmpty()) {
                    syncScope.launch {
                        database.transactionDao().upsertAll(remoteTransactions)
                    }
                }
            }

        val contractListener = householdDoc.collection("installment_contracts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val remoteContracts = snapshot.documents.mapNotNull { doc ->
                    try {
                        InstallmentContractEntity(
                            id = doc.getString("id") ?: doc.id,
                            description = doc.getString("description") ?: "",
                            monthlyAmountCents = doc.getLong("monthlyAmountCents") ?: 0L,
                            currency = CurrencyCode.valueOf(doc.getString("currency") ?: "BRL"),
                            startYear = (doc.getLong("startYear") ?: 2026L).toInt(),
                            startMonth = (doc.getLong("startMonth") ?: 1L).toInt(),
                            initialInstallmentNumber = (doc.getLong("initialInstallmentNumber") ?: 1L).toInt(),
                            totalInstallments = (doc.getLong("totalInstallments") ?: 1L).toInt(),
                            dayOfMonth = (doc.getLong("dayOfMonth") ?: 10L).toInt(),
                            categoryId = doc.getString("categoryId"),
                            type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE_BRAZIL"),
                            userId = doc.getString("userId") ?: "shared",
                            userDisplayName = doc.getString("userDisplayName") ?: "",
                            paidMonths = doc.getString("paidMonths") ?: "",
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            isDeleted = doc.getBoolean("isDeleted") ?: false,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteContracts.isNotEmpty()) {
                    syncScope.launch {
                        database.installmentContractDao().upsertAll(remoteContracts)
                    }
                }
            }

        val rateListener = householdDoc.collection("exchange_rates")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val remoteRates = snapshot.documents.mapNotNull { doc ->
                    try {
                        ExchangeRateEntity(
                            year = (doc.getLong("year") ?: 2026L).toInt(),
                            month = (doc.getLong("month") ?: 1L).toInt(),
                            eurToBrlRate = doc.getDouble("eurToBrlRate") ?: 6.44,
                            lastUpdatedDate = doc.getString("lastUpdatedDate")?.let(LocalDate::parse)
                                ?: LocalDate.now(),
                            updatedByUserId = doc.getString("updatedByUserId") ?: "shared",
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteRates.isNotEmpty()) {
                    syncScope.launch {
                        database.exchangeRateDao().upsertAll(remoteRates)
                    }
                }
            }

        val goalListener = householdDoc.collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val remoteGoals = snapshot.documents.mapNotNull { doc ->
                    try {
                        GoalEntity(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            targetAmountCents = doc.getLong("targetAmountCents") ?: 0L,
                            currentSavedCents = doc.getLong("currentSavedCents") ?: 0L,
                            currency = CurrencyCode.valueOf(doc.getString("currency") ?: "EUR"),
                            targetDate = doc.getString("targetDate")?.let(LocalDate::parse),
                            notes = doc.getString("notes") ?: "",
                            priority = GoalPriority.valueOf(doc.getString("priority") ?: "SHORT_TERM"),
                            bucketName = doc.getString("bucketName")?.takeIf { it.isNotBlank() },
                            updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis") ?: 0L,
                            isDeleted = doc.getBoolean("isDeleted") ?: false,
                            syncStatus = SyncStatus.SYNCED
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteGoals.isNotEmpty()) {
                    syncScope.launch {
                        database.goalDao().upsertAll(remoteGoals)
                    }
                }
            }

        activeListeners = listOf(
            categoryListener,
            bucketListener,
            txListener,
            contractListener,
            rateListener,
            goalListener
        )
    }

    suspend fun pushCategory(entity: CategoryEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val payload = mapOf(
                "id" to entity.id,
                "seriesId" to entity.seriesId,
                "year" to entity.year,
                "month" to entity.month,
                "name" to entity.name,
                "type" to entity.type.name,
                "envelopeName" to if (entity.type == TransactionType.INCOME) null else entity.envelopeName,
                "plannedAmountCents" to entity.plannedAmountCents,
                "plannedPercentage" to entity.plannedPercentage,
                "currency" to entity.currency.name,
                "iconKey" to entity.iconKey,
                "colorHex" to entity.colorHex,
                "sortOrder" to entity.sortOrder,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis,
                "isDeleted" to entity.isDeleted
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("categories")
                .document(entity.id)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore category sync for ${entity.id}", e)
            false
        }
    }

    suspend fun pushBudgetBucket(entity: BudgetBucketEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val docId = entity.bucketName.trim().lowercase().replace(Regex("[^a-z0-9_]+"), "_")
                .ifEmpty { "bucket" }
            val payload = mapOf(
                "bucketName" to entity.bucketName,
                "sortOrder" to entity.sortOrder,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis,
                "isDeleted" to entity.isDeleted
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("budget_buckets")
                .document(docId)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore bucket sync for ${entity.bucketName}", e)
            false
        }
    }

    suspend fun pushTransaction(entity: TransactionEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val payload = mapOf(
                "id" to entity.id,
                "description" to entity.description,
                "amountCents" to entity.amountCents,
                "currency" to entity.currency.name,
                "date" to entity.date.toString(),
                "year" to entity.year,
                "month" to entity.month,
                "categoryId" to entity.categoryId,
                "type" to entity.type.name,
                "userId" to entity.userId,
                "userDisplayName" to entity.userDisplayName,
                "isPaid" to entity.isPaid,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis,
                "isDeleted" to entity.isDeleted
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("transactions")
                .document(entity.id)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore transaction sync for ${entity.id}", e)
            false
        }
    }

    suspend fun pushInstallmentContract(entity: InstallmentContractEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val payload = mapOf(
                "id" to entity.id,
                "description" to entity.description,
                "monthlyAmountCents" to entity.monthlyAmountCents,
                "currency" to entity.currency.name,
                "startYear" to entity.startYear,
                "startMonth" to entity.startMonth,
                "initialInstallmentNumber" to entity.initialInstallmentNumber,
                "totalInstallments" to entity.totalInstallments,
                "dayOfMonth" to entity.dayOfMonth,
                "categoryId" to entity.categoryId,
                "type" to entity.type.name,
                "userId" to entity.userId,
                "userDisplayName" to entity.userDisplayName,
                "paidMonths" to entity.paidMonths,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis,
                "isDeleted" to entity.isDeleted
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("installment_contracts")
                .document(entity.id)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore installment sync for ${entity.id}", e)
            false
        }
    }

    suspend fun pushExchangeRate(entity: ExchangeRateEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val payload = mapOf(
                "year" to entity.year,
                "month" to entity.month,
                "eurToBrlRate" to entity.eurToBrlRate,
                "lastUpdatedDate" to entity.lastUpdatedDate.toString(),
                "updatedByUserId" to entity.updatedByUserId,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("exchange_rates")
                .document(entity.documentId)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore exchange rate sync for ${entity.documentId}", e)
            false
        }
    }

    suspend fun pushGoal(entity: GoalEntity): Boolean {
        val db = firestoreOrNull ?: return false
        return try {
            val payload = mapOf(
                "id" to entity.id,
                "title" to entity.title,
                "targetAmountCents" to entity.targetAmountCents,
                "currentSavedCents" to entity.currentSavedCents,
                "currency" to entity.currency.name,
                "targetDate" to entity.targetDate?.toString(),
                "notes" to entity.notes,
                "priority" to entity.priority.name,
                "bucketName" to entity.bucketName,
                "updatedAtEpochMillis" to entity.updatedAtEpochMillis,
                "isDeleted" to entity.isDeleted
            )
            db.collection("households")
                .document(currentHouseholdId)
                .collection("goals")
                .document(entity.id)
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Deferred Firestore goal sync for ${entity.id}", e)
            false
        }
    }

    companion object {
        private const val TAG = "FirestoreSyncDataSource"
    }
}
