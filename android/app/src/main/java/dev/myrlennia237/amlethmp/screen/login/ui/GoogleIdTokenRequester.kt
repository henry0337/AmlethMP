package dev.myrlennia237.amlethmp.screen.login.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.myrlennia237.amlethmp.BuildConfig

// Credential Manager needs an Activity context and shows its own sheet, so this is UI logic: the
// screen asks for the token here and hands only the resulting value to the ViewModel.
internal class GoogleIdTokenRequester(
    private val context: Context,
    private val serverClientId: String
) {

    suspend fun request(): Result<String> {
        if (serverClientId.isBlank()) {
            return Result.failure(IllegalStateException("GOOGLE_WEB_CLIENT_ID is not set"))
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
            .build()

        return try {
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleSignInCancelledException())
        } catch (e: NoCredentialException) {
            // No Google account on the device (or none usable by this app).
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(e)
        }
    }
}

@Composable
internal fun rememberGoogleIdTokenRequester(): GoogleIdTokenRequester {
    val context = LocalContext.current
    return remember(context) { GoogleIdTokenRequester(context, BuildConfig.GOOGLE_WEB_CLIENT_ID) }
}

internal class GoogleSignInCancelledException : Exception("Google sign-in was cancelled")
