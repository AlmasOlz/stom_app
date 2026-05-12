package com.example.stomatology.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.RoleRequestStatus
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.model.Clinic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AdminClinicForm(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val servicesInput: String = "",
    val priceFrom: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val latitude: String = "",
    val longitude: String = ""
)

data class AdminUserItem(
    val uid: String,
    val email: String,
    val displayName: String,
    val phone: String,
    val role: String,
    val requestedRole: String,
    val requestStatus: String,
    val specialty: String,
    val clinicId: String,
    val clinicName: String
)

data class AdminUserForm(
    val selectedUserId: String = "",
    val displayName: String = "",
    val phone: String = "",
    val role: String = UserRoles.PATIENT,
    val specialty: String = "",
    val clinicId: String = ""
)

data class AdminDashboardUiState(
    val selectedTab: Int = 0,
    val clinics: List<Clinic> = emptyList(),
    val users: List<AdminUserItem> = emptyList(),
    val clinicForm: AdminClinicForm = AdminClinicForm(),
    val userForm: AdminUserForm = AdminUserForm(),
    val isLoadingClinics: Boolean = true,
    val isLoadingUsers: Boolean = true,
    val isSavingClinic: Boolean = false,
    val isSavingUser: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val isLoading: Boolean
        get() = isLoadingClinics || isLoadingUsers
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState

    private var clinicsListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null

    init {
        observeClinics()
        observeUsers()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onClinicNameChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(name = value))
        }
    }

    fun onClinicAddressChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(address = value))
        }
    }

    fun onClinicServicesChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(servicesInput = value))
        }
    }

    fun onClinicPriceChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(priceFrom = value))
        }
    }

    fun onClinicDescriptionChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(description = value))
        }
    }

    fun onClinicImageUrlChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(imageUrl = value))
        }
    }

    fun onClinicLatitudeChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(latitude = value))
        }
    }

    fun onClinicLongitudeChange(value: String) {
        _uiState.update { state ->
            state.copy(clinicForm = state.clinicForm.copy(longitude = value))
        }
    }

    fun editClinic(clinic: Clinic) {
        _uiState.update { state ->
            state.copy(
                clinicForm = AdminClinicForm(
                    id = clinic.id,
                    name = clinic.name,
                    address = clinic.address,
                    servicesInput = clinic.services.joinToString(", "),
                    priceFrom = clinic.priceFrom.takeIf { it > 0 }?.toString().orEmpty(),
                    description = clinic.description,
                    imageUrl = clinic.imageUrl,
                    latitude = clinic.latitude.takeIf { it != 0.0 }?.toString().orEmpty(),
                    longitude = clinic.longitude.takeIf { it != 0.0 }?.toString().orEmpty()
                ),
                selectedTab = 0,
                error = null,
                message = null
            )
        }
    }

    fun clearClinicForm() {
        _uiState.update {
            it.copy(clinicForm = AdminClinicForm())
        }
    }

    fun saveClinic() {
        val form = _uiState.value.clinicForm
        val name = form.name.trim()
        val address = form.address.trim()
        val services = parseServices(form.servicesInput)
        val priceFrom = form.priceFrom.trim().toIntOrNull() ?: 0
        val description = form.description.trim()
        val imageUrl = form.imageUrl.trim()
        val latitude = form.latitude.trim().toDoubleOrNull() ?: 0.0
        val longitude = form.longitude.trim().toDoubleOrNull() ?: 0.0

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Клиника атауын енгізіңіз") }
            return
        }

        if (priceFrom < 0) {
            _uiState.update { it.copy(error = "Баға теріс болмауы керек") }
            return
        }

        if (form.latitude.isNotBlank() && (latitude < -90.0 || latitude > 90.0)) {
            _uiState.update { it.copy(error = "Ендік -90 мен 90 аралығында болуы керек") }
            return
        }

        if (form.longitude.isNotBlank() && (longitude < -180.0 || longitude > 180.0)) {
            _uiState.update { it.copy(error = "Бойлық -180 мен 180 аралығында болуы керек") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingClinic = true,
                    error = null,
                    message = null
                )
            }

            try {
                val clinicId = form.id.ifBlank {
                    firestore.collection(FirestoreCollections.CLINICS).document().id
                }
                val now = System.currentTimeMillis()

                val data = hashMapOf(
                    FirestoreFields.NAME to name,
                    FirestoreFields.ADDRESS to address,
                    FirestoreFields.SERVICES to services,
                    FirestoreFields.PRICE_FROM to priceFrom,
                    FirestoreFields.DESCRIPTION to description,
                    FirestoreFields.IMAGE_URL to imageUrl,
                    FirestoreFields.LATITUDE to latitude,
                    FirestoreFields.LONGITUDE to longitude,
                    FirestoreFields.RATING to 0.0,
                    FirestoreFields.REVIEWS to 0,
                    FirestoreFields.UPDATED_AT to now
                )

                if (form.id.isBlank()) {
                    data[FirestoreFields.CREATED_AT] = now
                }

                firestore.collection(FirestoreCollections.CLINICS)
                    .document(clinicId)
                    .set(data, SetOptions.merge())
                    .await()

                _uiState.update {
                    it.copy(
                        clinicForm = AdminClinicForm(),
                        isSavingClinic = false,
                        message = "Клиника сақталды"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingClinic = false,
                        error = e.message ?: "Клиниканы сақтау мүмкін болмады"
                    )
                }
            }
        }
    }

    fun deleteClinic(clinicId: String) {
        if (clinicId.isBlank()) return

        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.CLINICS)
                    .document(clinicId)
                    .delete()
                    .await()

                val currentForm = _uiState.value.clinicForm
                _uiState.update {
                    it.copy(
                        clinicForm = if (currentForm.id == clinicId) AdminClinicForm() else currentForm,
                        message = "Клиника жойылды",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Клиниканы жою мүмкін болмады")
                }
            }
        }
    }

    fun selectUser(user: AdminUserItem) {
        _uiState.update {
            it.copy(
                userForm = AdminUserForm(
                    selectedUserId = user.uid,
                    displayName = user.displayName,
                    phone = user.phone,
                    role = user.role,
                    specialty = user.specialty,
                    clinicId = user.clinicId
                ),
                selectedTab = 1,
                error = null,
                message = null
            )
        }
    }

    fun onUserDisplayNameChange(value: String) {
        _uiState.update { state ->
            state.copy(userForm = state.userForm.copy(displayName = value))
        }
    }

    fun onUserPhoneChange(value: String) {
        _uiState.update { state ->
            state.copy(userForm = state.userForm.copy(phone = value))
        }
    }

    fun onUserRoleChange(value: String) {
        _uiState.update { state ->
            state.copy(
                userForm = if (value == UserRoles.DOCTOR) {
                    state.userForm.copy(role = value)
                } else {
                    state.userForm.copy(
                        role = value,
                        specialty = "",
                        clinicId = ""
                    )
                }
            )
        }
    }

    fun onUserSpecialtyChange(value: String) {
        _uiState.update { state ->
            state.copy(userForm = state.userForm.copy(specialty = value))
        }
    }

    fun onUserClinicIdChange(value: String) {
        _uiState.update { state ->
            state.copy(userForm = state.userForm.copy(clinicId = value))
        }
    }

    fun saveUserSettings() {
        val form = _uiState.value.userForm
        val userId = form.selectedUserId

        if (userId.isBlank()) {
            _uiState.update { it.copy(error = "Алдымен пайдаланушыны таңдаңыз") }
            return
        }

        if (form.role == UserRoles.DOCTOR && form.specialty.isBlank()) {
            _uiState.update { it.copy(error = "Дәрігер рөлі үшін мамандықты көрсетіңіз") }
            return
        }

        if (form.role == UserRoles.DOCTOR && form.clinicId.isBlank()) {
            _uiState.update { it.copy(error = "Дәрігер үшін клиниканы таңдаңыз") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingUser = true,
                    error = null,
                    message = null
                )
            }

            try {
                val now = System.currentTimeMillis()
                val adminUid = auth.currentUser?.uid.orEmpty()
                val selectedUser = _uiState.value.users.firstOrNull { it.uid == userId }
                val selectedClinicName = _uiState.value.clinics
                    .firstOrNull { it.id == form.clinicId.trim() }
                    ?.name
                    .orEmpty()

                val data = hashMapOf(
                    FirestoreFields.ROLE to form.role,
                    FirestoreFields.REQUESTED_ROLE to form.role,
                    FirestoreFields.DISPLAY_NAME to form.displayName.trim(),
                    FirestoreFields.PHONE to form.phone.trim(),
                    FirestoreFields.SPECIALTY to form.specialty.trim(),
                    FirestoreFields.CLINIC_ID to form.clinicId.trim(),
                    FirestoreFields.CLINIC_NAME to selectedClinicName,
                    FirestoreFields.UPDATED_AT to now
                )

                if (form.role == UserRoles.DOCTOR) {
                    data[FirestoreFields.REQUEST_STATUS] = RoleRequestStatus.APPROVED
                    data[FirestoreFields.APPROVED_AT] = now
                    data[FirestoreFields.APPROVED_BY] = adminUid
                    data[FirestoreFields.IS_ACTIVE] = true
                } else if (selectedUser?.requestedRole == UserRoles.DOCTOR &&
                    selectedUser.requestStatus == RoleRequestStatus.PENDING
                ) {
                    data[FirestoreFields.ROLE] = UserRoles.PATIENT
                    data[FirestoreFields.REQUEST_STATUS] = RoleRequestStatus.REJECTED
                    data[FirestoreFields.REJECTED_AT] = now
                    data[FirestoreFields.REJECTED_BY] = adminUid
                    data[FirestoreFields.IS_ACTIVE] = false
                    data[FirestoreFields.SPECIALTY] = ""
                    data[FirestoreFields.CLINIC_ID] = ""
                    data[FirestoreFields.CLINIC_NAME] = ""
                } else {
                    data[FirestoreFields.REQUEST_STATUS] = RoleRequestStatus.NONE
                    data[FirestoreFields.IS_ACTIVE] = true
                }

                firestore.collection(FirestoreCollections.USERS)
                    .document(userId)
                    .set(data, SetOptions.merge())
                    .await()

                _uiState.update {
                    it.copy(
                        isSavingUser = false,
                        message = "Пайдаланушы жаңартылды"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingUser = false,
                        error = e.message ?: "Пайдаланушыны жаңарту мүмкін болмады"
                    )
                }
            }
        }
    }

    fun clearUserSelection() {
        _uiState.update {
            it.copy(userForm = AdminUserForm())
        }
    }

    fun clearNotifications() {
        _uiState.update { it.copy(error = null, message = null) }
    }

    fun retryLoadData() {
        clearNotifications()
        observeClinics()
        observeUsers()
    }

    private fun observeClinics() {
        clinicsListener?.remove()
        _uiState.update { it.copy(isLoadingClinics = true) }

        clinicsListener = firestore.collection(FirestoreCollections.CLINICS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoadingClinics = false,
                            error = error.message ?: "Клиникаларды жүктеу мүмкін болмады"
                        )
                    }
                    return@addSnapshotListener
                }

                val clinics = snapshot?.documents
                    ?.map { document -> document.toClinic() }
                    ?.sortedBy { clinic -> clinic.name.lowercase() }
                    ?: emptyList()

                _uiState.update {
                    it.copy(
                        clinics = clinics,
                        isLoadingClinics = false
                    )
                }
            }
    }

    private fun observeUsers() {
        usersListener?.remove()
        _uiState.update { it.copy(isLoadingUsers = true) }

        usersListener = firestore.collection(FirestoreCollections.USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoadingUsers = false,
                            error = error.message ?: "Пайдаланушыларды жүктеу мүмкін болмады"
                        )
                    }
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.map { document -> document.toAdminUser() }
                    ?.sortedWith(
                        compareBy<AdminUserItem> { user ->
                            user.requestStatus != RoleRequestStatus.PENDING
                        }.thenBy { user ->
                            user.displayName.lowercase()
                        }
                    )
                    ?: emptyList()

                _uiState.update {
                    it.copy(
                        users = users,
                        isLoadingUsers = false
                    )
                }
            }
    }

    private fun DocumentSnapshot.toClinic(): Clinic {
        val services = (get(FirestoreFields.SERVICES) as? List<*>)
            ?.mapNotNull { value -> value as? String }
            ?: emptyList()
        val priceFrom = parseIntValue(get(FirestoreFields.PRICE_FROM))
            ?: extractMinPriceFromPriceList(get(FirestoreFields.PRICE_LIST))
            ?: 0

        return Clinic(
            id = id,
            name = getString(FirestoreFields.NAME).orEmpty(),
            rating = getDouble(FirestoreFields.RATING) ?: 0.0,
            reviews = getLong(FirestoreFields.REVIEWS)?.toInt() ?: 0,
            address = getString(FirestoreFields.ADDRESS).orEmpty(),
            phone = getString(FirestoreFields.PHONE).orEmpty(),
            services = services,
            imageUrl = getString(FirestoreFields.IMAGE_URL).orEmpty(),
            priceFrom = priceFrom,
            description = getString(FirestoreFields.DESCRIPTION).orEmpty(),
            latitude = getDouble(FirestoreFields.LATITUDE) ?: 0.0,
            longitude = getDouble(FirestoreFields.LONGITUDE) ?: 0.0
        )
    }

    private fun DocumentSnapshot.toAdminUser(): AdminUserItem {
        val firstName = getString(FirestoreFields.FIRST_NAME).orEmpty()
        val lastName = getString(FirestoreFields.LAST_NAME).orEmpty()
        val displayName = getString(FirestoreFields.DISPLAY_NAME)
            ?.takeIf { value -> value.isNotBlank() }
            ?: "$firstName $lastName".trim()

        return AdminUserItem(
            uid = getString(FirestoreFields.UID) ?: id,
            email = getString(FirestoreFields.EMAIL).orEmpty(),
            displayName = displayName.ifBlank { "Без имени" },
            phone = getString(FirestoreFields.PHONE).orEmpty(),
            role = getString(FirestoreFields.ROLE) ?: UserRoles.PATIENT,
            requestedRole = getString(FirestoreFields.REQUESTED_ROLE)
                ?: getString(FirestoreFields.ROLE)
                ?: UserRoles.PATIENT,
            requestStatus = getString(FirestoreFields.REQUEST_STATUS) ?: RoleRequestStatus.NONE,
            specialty = getString(FirestoreFields.SPECIALTY).orEmpty(),
            clinicId = getString(FirestoreFields.CLINIC_ID).orEmpty(),
            clinicName = getString(FirestoreFields.CLINIC_NAME).orEmpty()
        )
    }

    private fun parseServices(raw: String): List<String> {
        return raw
            .split(",", "\n", ";", "|")
            .map { value -> value.trim() }
            .filter { value -> value.isNotBlank() }
            .distinct()
    }

    override fun onCleared() {
        clinicsListener?.remove()
        usersListener?.remove()
        super.onCleared()
    }
}

private fun parseIntValue(value: Any?): Int? {
    return when (value) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }?.takeIf { it >= 0 }
}

private fun extractMinPriceFromPriceList(raw: Any?): Int? {
    val list = raw as? List<*> ?: return null
    val prices = list.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        parseIntValue(map["price"])
    }
    return prices.minOrNull()
}
