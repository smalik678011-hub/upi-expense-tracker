package com.example.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    AuthScaffold(
        title = "Welcome back",
        subtitle = "Login karke apne UPI expenses secure rakho."
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = viewModel::sendPasswordReset,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }
        AuthMessages(state)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.signIn(onAuthSuccess) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) CircularProgressIndicator() else Text("Login")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { viewModel.continueAsGuest(onAuthSuccess) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue as guest")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToSignup, modifier = Modifier.fillMaxWidth()) {
            Text("New user? Create account")
        }
    }
}

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    AuthScaffold(
        title = "Create account",
        subtitle = "Signup karke backup aur future cloud sync ready rakho."
    ) {
        OutlinedTextField(
            value = state.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = { Text("Name optional") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        AuthMessages(state)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.signUp(onAuthSuccess) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) CircularProgressIndicator() else Text("Create account")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "UPI Expense Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(28.dp))
            content()
        }
    }
}

@Composable
private fun AuthMessages(state: AuthUiState) {
    state.errorMessage?.let {
        Spacer(Modifier.height(12.dp))
        Text(text = it, color = MaterialTheme.colorScheme.error)
    }
    state.infoMessage?.let {
        Spacer(Modifier.height(12.dp))
        Text(text = it, color = MaterialTheme.colorScheme.primary)
    }
}
