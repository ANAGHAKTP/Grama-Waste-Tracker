package com.grama.wastetracker.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.grama.wastetracker.data.model.UserProfile
import com.grama.wastetracker.data.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Repository for Firebase Phone/OTP Authentication and user profile management.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Observe auth state changes as a Flow of nullable FirebaseUser.
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Send OTP to the given phone number.
     * Returns the verification ID via callback, or auto-verifies if possible.
     */
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (credential: PhoneAuthCredential) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification (e.g., Google Play auto-read SMS)
                onAutoVerified(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verify the OTP code using the verification ID.
     */
    suspend fun verifyOtp(verificationId: String, code: String): UserProfile {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return signInWithCredential(credential)
    }

    /**
     * Sign in with a PhoneAuthCredential (from OTP or auto-verify).
     * Creates a Firestore user profile if one doesn't exist.
     */
    suspend fun signInWithCredential(credential: PhoneAuthCredential): UserProfile {
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Sign-in succeeded but user is null")
        return getOrCreateProfile(user)
    }

    /**
     * Fetch the user profile from Firestore, creating it if it doesn't exist.
     */
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

    /**
     * Fetch the current user's profile from Firestore.
     */
    suspend fun getCurrentProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(uid).get().await()
        return snapshot.toObject(UserProfile::class.java)
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Returns the current Firebase user, or null.
     */
    fun currentUser(): FirebaseUser? = auth.currentUser
}
