package rs.edu.raf.rma.movies.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
) {
    val state by viewModel.state.collectAsState()

    AuthScreen(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScreen(
    state: AuthContract.UiState,
    eventPublisher: (AuthContract.UiEvent) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Showtime",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        eventPublisher(AuthContract.UiEvent.ClearError)
                    },
                    text = { Text("Login") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        eventPublisher(AuthContract.UiEvent.ClearError)
                    },
                    text = { Text("Register") },
                )
            }

            when (selectedTab) {
                0 -> LoginForm(state = state, eventPublisher = eventPublisher)
                1 -> RegisterForm(state = state, eventPublisher = eventPublisher)
            }
        }
    }
}

@Composable
private fun LoginForm(
    state: AuthContract.UiState,
    eventPublisher: (AuthContract.UiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.loginUsername,
                onValueChange = { eventPublisher(AuthContract.UiEvent.UpdateLoginUsername(it)) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null,
            )

            OutlinedTextField(
                value = state.loginPassword,
                onValueChange = { eventPublisher(AuthContract.UiEvent.UpdateLoginPassword(it)) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.error != null,
            )

            if (state.error != null) {
                Text(
                    text = state.error.message ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = { eventPublisher(AuthContract.UiEvent.SubmitLogin) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.loginFormValid && !state.isLoading,
            ) {
                Text("Login")
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Signing in...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterForm(
    state: AuthContract.UiState,
    eventPublisher: (AuthContract.UiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.registerFullName,
                onValueChange = { eventPublisher(AuthContract.UiEvent.UpdateRegisterFullName(it)) },
                label = { Text("Full Name") },
                supportingText = { Text("Required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null,
            )

            OutlinedTextField(
                value = state.registerUsername,
                onValueChange = { eventPublisher(AuthContract.UiEvent.UpdateRegisterUsername(it)) },
                label = { Text("Username") },
                supportingText = { Text("Min 3 characters: letters, digits, underscore") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null,
            )

            OutlinedTextField(
                value = state.registerPassword,
                onValueChange = { eventPublisher(AuthContract.UiEvent.UpdateRegisterPassword(it)) },
                label = { Text("Password") },
                supportingText = { Text("Min 8 characters") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.error != null,
            )

            if (state.error != null) {
                Text(
                    text = state.error.message ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = { eventPublisher(AuthContract.UiEvent.SubmitRegister) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.registerFormValid && !state.isLoading,
            ) {
                Text("Create Account")
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Creating account...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}