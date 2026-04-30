package com.example.stomatology.app.presentation.profile

import com.example.stomatology.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.data.remote.CloudinaryApi
import com.example.stomatology.app.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class UserProfileUiState(
    val user: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val saveCompleted: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinaryApi: CloudinaryApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState

    private var profileListener: ListenerRegistration? = null

    init {
        observeUserProfile()
    }

    fun loadUserProfile() {
        observeUserProfile(forceReload = true)
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        specialty: String = "",
        experienceYears: Int = 0,
        aboutDoctor: String = ""
    ) {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanPhone = phone.trim()
        val cleanSpecialty = specialty.trim()
        val cleanAboutDoctor = aboutDoctor.trim()

        if (cleanFirstName.isBlank()) {
            _uiState.update { it.copy(error = "Атыңызды енгізіңіз") }
            return
        }

        if (cleanPhone.isNotBlank() && cleanPhone.length < 6) {
            _uiState.update { it.copy(error = "Телефон нөмірі тым қысқа") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveCompleted = false,
                    error = null,
                    message = null
                )
            }

            try {
                val uid = currentUid()
                val displayName = listOf(cleanFirstName, cleanLastName)
                    .filter { value -> value.isNotBlank() }
                    .joinToString(" ")

                val data = hashMapOf(
                    FirestoreFields.FIRST_NAME to cleanFirstName,
                    FirestoreFields.LAST_NAME to cleanLastName,
                    FirestoreFields.DISPLAY_NAME to displayName,
                    FirestoreFields.PHONE to cleanPhone,
                    FirestoreFields.SPECIALTY to cleanSpecialty,
                    FirestoreFields.EXPERIENCE_YEARS to experienceYears.coerceAtLeast(0),
                    FirestoreFields.ABOUT_DOCTOR to cleanAboutDoctor,
                    FirestoreFields.UPDATED_AT to System.currentTimeMillis()
                )

                firestore.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .await()

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveCompleted = true,
                        message = "Профиль сәтті сақталды"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Профильді сақтау мүмкін болмады"
                    )
                }
            }
        }
    }

    fun uploadProfilePhoto(
        fileBytes: ByteArray,
        mimeType: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingPhoto = true,
                    error = null,
                    message = null
                )
            }

            try {
                val uid = currentUid()
                val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME.trim()
                val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.trim()
                if (cloudName.isBlank() || uploadPreset.isBlank()) {
                    throw IllegalStateException("Cloudinary бапталмаған. gradle.properties ішіне cloudinaryCloudName және cloudinaryUploadPreset қосыңыз.")
                }

                val safeMime = mimeType.ifBlank { "image/jpeg" }
                val fileRequest = fileBytes.toRequestBody(safeMime.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "${uid}_avatar.jpg",
                    body = fileRequest
                )
                val presetPart = uploadPreset.toRequestBody("text/plain".toMediaTypeOrNull())

                val uploadResponse = cloudinaryApi.uploadImage(
                    cloudName = cloudName,
                    file = filePart,
                    uploadPreset = presetPart
                )
                val photoUrl = uploadResponse.secureUrl
                    ?: uploadResponse.url
                    ?: throw IllegalStateException("Cloudinary URL қайтармады")

                firestore.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .set(
                        mapOf(
                            FirestoreFields.PHOTO_URL to photoUrl,
                            FirestoreFields.UPDATED_AT to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()

                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        message = "Фото жаңартылды"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        error = mapPhotoUploadError(e)
                    )
                }
            }
        }
    }

    fun savePhotoUrl(photoUrl: String) {
        val cleanUrl = photoUrl.trim()
        if (cleanUrl.isBlank()) {
            _uiState.update { it.copy(error = "Фото сілтемесін енгізіңіз") }
            return
        }
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            _uiState.update { it.copy(error = "Сілтеме http:// немесе https:// арқылы басталуы керек") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                val uid = currentUid()
                firestore.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .set(
                        mapOf(
                            FirestoreFields.PHOTO_URL to cleanUrl,
                            FirestoreFields.UPDATED_AT to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Фото сілтемесі сақталды"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Фото сілтемесін сақтау мүмкін болмады"
                    )
                }
            }
        }
    }

    fun clearSaveEvent() {
        _uiState.update { it.copy(saveCompleted = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(error = message, message = null) }
    }

    private fun observeUserProfile(forceReload: Boolean = false) {
        if (profileListener != null && !forceReload) {
            return
        }

        profileListener?.remove()
        _uiState.update { it.copy(isLoading = true, error = null) }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Пайдаланушы авторизациядан өтпеген"
                )
            }
            return
        }

        profileListener = firestore.collection(FirestoreCollections.USERS)
            .document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Профиль жүктелмеді"
                        )
                    }
                    return@addSnapshotListener
                }

                val profile = snapshot?.toObject(UserProfile::class.java)
                    ?: UserProfile(
                        uid = currentUser.uid,
                        email = currentUser.email.orEmpty()
                    )

                _uiState.update {
                    it.copy(
                        user = profile.copy(
                            uid = profile.uid.ifBlank { currentUser.uid },
                            email = profile.email.ifBlank { currentUser.email.orEmpty() }
                        ),
                        isLoading = false,
                        error = null
                    )
                }
            }
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Пайдаланушы авторизациядан өтпеген")
    }

    private fun mapPhotoUploadError(e: Exception): String {
        val lower = e.message.orEmpty().lowercase()
        return when {
            lower.contains("cloudinary бапталмаған") -> {
                "Cloudinary бапталмаған. gradle.properties ішіне cloudinaryCloudName және cloudinaryUploadPreset қосыңыз."
            }

            lower.contains("401") || lower.contains("403") || lower.contains("unauthorized") -> {
                "Cloudinary рұқсаты қате. cloudName/preset мәндерін тексеріңіз."
            }

            lower.contains("timeout") || lower.contains("failed to connect") || lower.contains("unable to resolve host") -> {
                "Желі қатесі. Интернетті тексеріп, қайта көріңіз."
            }

            else -> e.message ?: "Фото жүктеу мүмкін болмады"
        }
    }

    override fun onCleared() {
        profileListener?.remove()
        super.onCleared()
    }
}
