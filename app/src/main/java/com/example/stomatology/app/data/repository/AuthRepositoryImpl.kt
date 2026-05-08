package com.example.stomatology.app.data.repository

import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.RoleRequestStatus
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.repository.AuthRepository
import com.example.stomatology.app.domain.repository.DoctorRequestInfo
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
        phone: String,
        requestedRole: String,
        specialty: String,
        clinicId: String
    ): Result<Boolean> {
        val authResult = try {
            firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val user = authResult.user
            ?: return Result.failure(Exception("UID not found"))
        val uid = user.uid

        return try {
            val displayName = "$firstName $lastName".trim()
            val now = System.currentTimeMillis()
            val normalizedRequestedRole = if (requestedRole == UserRoles.DOCTOR) {
                UserRoles.DOCTOR
            } else {
                UserRoles.PATIENT
            }
            val requestStatus = if (normalizedRequestedRole == UserRoles.DOCTOR) {
                RoleRequestStatus.PENDING
            } else {
                RoleRequestStatus.NONE
            }
            val normalizedClinicId = clinicId.trim()
            val clinicName = if (normalizedRequestedRole == UserRoles.DOCTOR && normalizedClinicId.isNotBlank()) {
                runCatching {
                    firestore.collection(FirestoreCollections.CLINICS)
                        .document(normalizedClinicId)
                        .get()
                        .await()
                        .getString(FirestoreFields.NAME)
                        .orEmpty()
                }.getOrDefault("")
            } else {
                ""
            }

            val userData = hashMapOf(
                FirestoreFields.UID to uid,
                FirestoreFields.AUTH_UID to uid,
                FirestoreFields.EMAIL to email,
                FirestoreFields.ROLE to UserRoles.PATIENT,
                FirestoreFields.REQUESTED_ROLE to normalizedRequestedRole,
                FirestoreFields.REQUEST_STATUS to requestStatus,
                FirestoreFields.FIRST_NAME to firstName,
                FirestoreFields.LAST_NAME to lastName,
                FirestoreFields.DISPLAY_NAME to displayName,
                FirestoreFields.PHONE to phone,
                FirestoreFields.SPECIALTY to specialty.trim(),
                FirestoreFields.CLINIC_ID to normalizedClinicId,
                FirestoreFields.CLINIC_NAME to clinicName,
                FirestoreFields.IS_ACTIVE to (normalizedRequestedRole != UserRoles.DOCTOR),
                FirestoreFields.CREATED_AT to now,
                FirestoreFields.UPDATED_AT to now
            )

            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .set(userData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            runCatching {
                firebaseAuth.currentUser?.delete()?.await()
            }
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
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()

            val role = snapshot.getString(FirestoreFields.ROLE)
                ?: return Result.failure(Exception("Role not found"))

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDoctorRequestInfo(uid: String): Result<DoctorRequestInfo> {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()

            val requestedRole = snapshot.getString(FirestoreFields.REQUESTED_ROLE)
                ?: UserRoles.PATIENT
            val requestStatus = snapshot.getString(FirestoreFields.REQUEST_STATUS)
                ?: RoleRequestStatus.NONE

            Result.success(
                DoctorRequestInfo(
                    requestedRole = requestedRole,
                    requestStatus = requestStatus
                )
            )
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
                FirestoreFields.UID to uid,
                FirestoreFields.EMAIL to email,
                FirestoreFields.ROLE to role,
                FirestoreFields.FIRST_NAME to firstName,
                FirestoreFields.LAST_NAME to lastName,
                FirestoreFields.DISPLAY_NAME to "$firstName $lastName",
                FirestoreFields.PHONE to phone,
                FirestoreFields.CREATED_AT to System.currentTimeMillis()
            )

            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
