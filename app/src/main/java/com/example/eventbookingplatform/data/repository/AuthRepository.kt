package com.example.eventbookingplatform.data.repository

import com.example.eventbookingplatform.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            listener(firebaseAuth.currentUser)
        }
    }

    suspend fun register(name: String, email: String, password: String): UserProfile {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Registration succeeded but user id is missing.")
        val profile = UserProfile(name = trimmedName, email = trimmedEmail)
        firestore.collection(UserProfile.COLLECTION_USERS)
            .document(uid)
            .set(profile.toMap())
            .await()
        return profile
    }

    suspend fun login(email: String, password: String): UserProfile {
        val trimmedEmail = email.trim()
        auth.signInWithEmailAndPassword(trimmedEmail, password).await()
        val user = auth.currentUser
            ?: throw IllegalStateException("Login succeeded but no user session was created.")
        return ensureUserProfile(user)
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun fetchUserProfile(uid: String): UserProfile? {
        val document = firestore.collection(UserProfile.COLLECTION_USERS)
            .document(uid)
            .get()
            .await()
        return if (document.exists()) UserProfile.fromDocument(document) else null
    }

    suspend fun ensureUserProfile(user: FirebaseUser, name: String? = null): UserProfile {
        fetchUserProfile(user.uid)?.let { return it }
        val profile = UserProfile(
            name = name?.trim()?.takeIf { it.isNotEmpty() }
                ?: user.displayName?.trim()?.takeIf { it.isNotEmpty() }
                ?: user.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
                ?: "User",
            email = user.email.orEmpty(),
        )
        firestore.collection(UserProfile.COLLECTION_USERS)
            .document(user.uid)
            .set(profile.toMap())
            .await()
        return profile
    }
}
