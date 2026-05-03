package com.grama.wastetracker.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.grama.wastetracker.data.model.UserProfile
import com.grama.wastetracker.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(webClientId: String): Result<UserProfile> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleSignIn(result)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleSignIn(result: androidx.credentials.GetCredentialResponse): Result<UserProfile> {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

            return try {
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("Sign-in succeeded but user is null")
                val profile = getOrCreateProfile(user)
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        return Result.failure(Exception("Invalid credential type"))
    }

    suspend fun getOrCreateProfile(user: FirebaseUser): UserProfile {
        val docRef = db.collection("users").document(user.uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java) ?: UserProfile(uid = user.uid)
        } else {
            val newProfile = UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                phoneNumber = user.phoneNumber,
                role = UserRole.CITIZEN.value,
                createdAt = Instant.now().toString()
            )
            docRef.set(newProfile).await()
            newProfile
        }
    }

    suspend fun getCurrentProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(uid).get().await()
        return snapshot.toObject(UserProfile::class.java)
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        auth.signOut()
    }
}
