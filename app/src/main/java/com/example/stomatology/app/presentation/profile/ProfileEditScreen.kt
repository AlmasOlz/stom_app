package com.example.stomatology.app.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.R
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit = onBack,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val photoReadErrorText = stringResource(R.string.profile_photo_read_error)
    val user = state.user
    val isDoctor = user.role == UserRoles.DOCTOR

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var experienceYearsText by rememberSaveable { mutableStateOf("") }
    var aboutDoctor by rememberSaveable { mutableStateOf("") }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var localPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            localPhotoUri = it
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(it)?.use { stream -> stream.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                viewModel.showError(photoReadErrorText)
                return@let
            }
            val mimeType = resolver.getType(it).orEmpty()
            viewModel.uploadProfilePhoto(bytes, mimeType)
        }
    }

    LaunchedEffect(user.uid) {
        if (!initialized && user.uid.isNotBlank()) {
            firstName = user.firstName
            lastName = user.lastName
            phone = user.phone
            specialty = user.specialty
            experienceYearsText = user.experienceYears.takeIf { years -> years > 0 }?.toString().orEmpty()
            aboutDoctor = user.aboutDoctor
            initialized = true
        }
    }

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            viewModel.clearSaveEvent()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit), fontWeight = FontWeight.Bold) },
                navigationIcon = { AppBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
                return@Column
            }

            EditableAvatar(
                photoUrl = user.photoUrl,
                localPhotoUri = localPhotoUri,
                displayName = user.displayName.ifBlank { "$firstName $lastName".trim() },
                isUploading = state.isUploadingPhoto,
                onClick = { photoPicker.launch("image/*") }
            )

            TextButton(
                onClick = { photoPicker.launch("image/*") },
                enabled = !state.isUploadingPhoto
            ) {
                Text(stringResource(R.string.profile_change_photo), color = PrimaryBlue)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.profile_personal_info),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    ProfileEditField(firstName, { firstName = it }, stringResource(R.string.profile_first_name))
                    ProfileEditField(lastName, { lastName = it }, stringResource(R.string.profile_last_name))
                    ProfileEditField(user.email, {}, stringResource(R.string.profile_email), enabled = false)
                    ProfileEditField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = stringResource(R.string.profile_phone),
                        keyboardType = KeyboardType.Phone
                    )

                    if (isDoctor) {
                        HorizontalDivider()
                        Text(
                            stringResource(R.string.profile_doctor_profile),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        ProfileEditField(specialty, { specialty = it }, stringResource(R.string.profile_specialty))
                        ProfileEditField(
                            value = experienceYearsText,
                            onValueChange = { value -> experienceYearsText = value.filter { ch -> ch.isDigit() } },
                            label = stringResource(R.string.profile_experience_years),
                            keyboardType = KeyboardType.Number
                        )
                        ProfileEditField(
                            value = aboutDoctor,
                            onValueChange = { aboutDoctor = it },
                            label = stringResource(R.string.profile_about_doctor),
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }
            }

            state.error?.let { error ->
                MessageCard(error, Color(0xFFFFEBEE), Color(0xFFC62828))
            }

            state.message?.let { message ->
                MessageCard(message, Color(0xFFE8F5E9), Color(0xFF2E7D32))
            }

            Button(
                onClick = {
                    viewModel.updateProfile(
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        specialty = specialty,
                        experienceYears = experienceYearsText.toIntOrNull() ?: 0,
                        aboutDoctor = aboutDoctor
                    )
                },
                enabled = !state.isSaving && !state.isUploadingPhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.action_save),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EditableAvatar(
    photoUrl: String,
    localPhotoUri: Uri?,
    displayName: String,
    isUploading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0xFFE1F5FE))
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val imageModel = localPhotoUri ?: photoUrl.takeIf { value -> value.isNotBlank() }
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = profileEditInitials(displayName),
                color = PrimaryBlue,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_change_photo),
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            color = Color(0xFF5F6368),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue,
                disabledTextColor = Color.DarkGray,
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = Color.Gray
            )
        )
    }
}

@Composable
private fun MessageCard(
    text: String,
    background: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(textColor, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp
            )
        }
    }
}

private fun profileEditInitials(displayName: String): String {
    val parts = displayName
        .split(" ")
        .filter { value -> value.isNotBlank() }

    return parts
        .take(2)
        .joinToString("") { value -> value.first().uppercaseChar().toString() }
        .ifBlank { "Қ" }
}
