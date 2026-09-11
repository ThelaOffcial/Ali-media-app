package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Thin wrapper around Firebase Auth.
 * The live site + RTDB security rules require auth != null for every write.
 * We sign in anonymously so the Android app can call the exact same paths
 * (elephant_posts, post_likes, comments, elephant_likes, …) that the web app uses.
 */
class FirebaseAuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val uid: String?
        get() = auth.currentUser?.uid

    /**
     * Ensures a signed-in user exists. Prefers existing session; otherwise
     * creates an anonymous account (matches the public web experience).
     */
    suspend fun ensureSignedIn(): FirebaseUser = withContext(Dispatchers.IO) {
        val existing = auth.currentUser
        if (existing != null) {
            // Force-refresh token so writes don't fail with expired credentials
            try {
                existing.getIdToken(true).await()
            } catch (e: Exception) {
                Log.w(TAG, "Token refresh failed, continuing with current session", e)
            }
            return@withContext existing
        }
        val result = auth.signInAnonymously().await()
        val user = result.user ?: error("Anonymous sign-in returned null user")
        Log.i(TAG, "Signed in anonymously as ${user.uid}")
        user
    }

    /**
     * Returns a fresh ID token for RTDB REST calls (`?auth=TOKEN`).
     * Returns null if sign-in fails (reads still work without auth).
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            val user = ensureSignedIn()
            user.getIdToken(forceRefresh).await().token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to obtain ID token", e)
            null
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthService"
        val instance by lazy { FirebaseAuthService() }
    }
}
