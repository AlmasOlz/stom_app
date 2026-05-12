package com.example.stomatology.app.presentation.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.presentation.components.AppErrorState
import com.example.stomatology.app.presentation.components.AppLoadingState
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminDashboardScreen(
    initialTab: Int = 0,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(initialTab) {
        viewModel.onTabSelected(initialTab)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Әкімші панелі",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        containerColor = Color(0xFFF5F7FB)
    ) { padding ->
        when {
            state.isLoading -> {
                AppLoadingState(
                    message = "Деректер жүктелуде...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            state.error != null && state.clinics.isEmpty() && state.users.isEmpty() -> {
                AppErrorState(
                    message = "Деректерді жүктеу кезінде қате пайда болды.",
                    actionText = "Қайталап көру",
                    onAction = viewModel::retryLoadData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    state.error?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                        ) {
                            Text(
                                text = "Қате пайда болды. Қайталап көріңіз.",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    state.message?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7EF))
                        ) {
                            Text(
                                text = message,
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    AdminSummaryRow(state = state)
                    Spacer(modifier = Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = Color.White
                    ) {
                        Tab(
                            selected = state.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) },
                            text = { Text("Клиникалар") }
                        )
                        Tab(
                            selected = state.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) },
                            text = { Text("Қолданушылар") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.selectedTab == 0) {
                        ClinicsTab(
                            state = state,
                            onNameChange = viewModel::onClinicNameChange,
                            onAddressChange = viewModel::onClinicAddressChange,
                            onServicesChange = viewModel::onClinicServicesChange,
                            onPriceChange = viewModel::onClinicPriceChange,
                            onDescriptionChange = viewModel::onClinicDescriptionChange,
                            onImageUrlChange = viewModel::onClinicImageUrlChange,
                            onLatitudeChange = viewModel::onClinicLatitudeChange,
                            onLongitudeChange = viewModel::onClinicLongitudeChange,
                            onSave = viewModel::saveClinic,
                            onClear = viewModel::clearClinicForm,
                            onEdit = viewModel::editClinic,
                            onDelete = { clinicId -> viewModel.deleteClinic(clinicId) }
                        )
                    } else {
                        UsersTab(
                            state = state,
                            clinics = state.clinics,
                            onSelectUser = viewModel::selectUser,
                            onDisplayNameChange = viewModel::onUserDisplayNameChange,
                            onPhoneChange = viewModel::onUserPhoneChange,
                            onRoleChange = viewModel::onUserRoleChange,
                            onSpecialtyChange = viewModel::onUserSpecialtyChange,
                            onClinicIdChange = viewModel::onUserClinicIdChange,
                            onSaveUser = viewModel::saveUserSettings,
                            onClearUser = viewModel::clearUserSelection
                        )
                    }
                }
            }
        }
    }
}
