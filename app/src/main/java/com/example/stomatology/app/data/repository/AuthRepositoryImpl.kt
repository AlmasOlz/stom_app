package com.example.stomatology.app.data.repository

import com.example.stomatology.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Boolean> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("UID not found"))

            val displayName = "$firstName $lastName"

            val userData = hashMapOf(
                "uid" to uid,
                "email" to email,
                "role" to "patient",
                "firstName" to firstName,
                "lastName" to lastName,
                "displayName" to displayName,
                "phone" to phone,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .set(userData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun getUserRole(uid: String): Result<String> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val role = snapshot.getString("role")
                ?: return Result.failure(Exception("Role not found"))

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(
        uid: String,
        email: String,
        role: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "uid" to uid,
                "email" to email,
                "role" to role,
                "firstName" to firstName,
                "lastName" to lastName,
                "displayName" to "$firstName $lastName",
                "phone" to phone,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}