package com.lrds.finner.ui.transaction

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.lrds.finner.core.model.HouseholdSession
import com.lrds.finner.ui.theme.FinnerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSyncDialog(
    session: HouseholdSession,
    configuredWebClientId: String?,
    feedbackMessage: String?,
    onDismiss: () -> Unit,
    onSaveProfileAndWorkspace: (displayName: String, householdId: String, householdName: String) -> Unit,
    onSignInWithFirebaseEmail: (email: String, password: String, displayName: String, householdId: String) -> Unit,
    onSignInWithGoogle: (activityContext: Context, webClientId: String?, displayName: String, householdId: String) -> Unit,
    onSignOut: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activityContext = LocalContext.current
    var displayName by remember(session.userDisplayName) { mutableStateOf(session.userDisplayName) }
    var householdName by remember(session.householdName) { mutableStateOf(session.householdName) }
    var householdId by remember(session.householdId) { mutableStateOf(session.householdId) }
    var email by remember(session.userEmail) { mutableStateOf(session.userEmail ?: "") }
    var password by remember { mutableStateOf("") }
    var webClientIdInput by remember(configuredWebClientId) { mutableStateOf(configuredWebClientId ?: "") }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        shape = shapes.bottomSheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.sheetHorizontal, vertical = spacing.sheetVertical),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = "Firebase Auth & Workspace (lrds-finner)",
                style = typography.titleLarge,
                color = colors.onSurface,
            )
            Text(
                text = "Share the same Workspace Code (Household ID) across devices to synchronize in real time via Cloud Firestore.",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = householdName,
                onValueChange = { householdName = it },
                label = { Text("Budget Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = householdId,
                onValueChange = { householdId = it },
                label = { Text("Workspace Code (Household ID)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = colors.cardBorder)

            if (session.isFirebaseAuthenticated) {
                Text(
                    text = "Signed in to Firebase: ${session.userEmail ?: session.userId}",
                    style = typography.bodyMediumBold,
                    color = colors.primary,
                )
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign Out of Firebase")
                }
            } else {
                Text(
                    text = "1. Google Sign-In",
                    style = typography.labelMediumBold,
                    color = colors.onSurface,
                )
                if (configuredWebClientId.isNullOrBlank()) {
                    OutlinedTextField(
                        value = webClientIdInput,
                        onValueChange = { webClientIdInput = it },
                        label = { Text("Web Client ID (Firebase OAuth 2.0)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Button(
                    onClick = {
                        onSignInWithGoogle(
                            activityContext,
                            webClientIdInput.ifBlank { configuredWebClientId },
                            displayName,
                            householdId,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign in with Google")
                }

                HorizontalDivider(color = colors.cardBorder)

                Text(
                    text = "2. Email & Password (Firebase Auth)",
                    style = typography.labelMediumBold,
                    color = colors.onSurface,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min. 6 characters)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = {
                        if (email.isNotBlank() && password.length >= 6) {
                            onSignInWithFirebaseEmail(email, password, displayName, householdId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign In / Create Account with Email")
                }
            }

            if (!feedbackMessage.isNullOrBlank()) {
                Text(
                    text = feedbackMessage,
                    style = typography.bodySmall,
                    color = colors.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Close")
                }
                Button(
                    onClick = {
                        onSaveProfileAndWorkspace(displayName, householdId, householdName)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Workspace")
                }
            }

            Spacer(modifier = Modifier.height(spacing.sheetBottomPadding))
        }
    }
}
