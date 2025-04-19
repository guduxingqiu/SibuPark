package sibu.park.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sibu.park.ui.components.ErrorText
import sibu.park.ui.components.LoadingIndicator
import sibu.park.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.state.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // 监听重置密码邮件发送成功
    LaunchedEffect(key1 = authState.isResetPasswordEmailSent) {
        if (authState.isResetPasswordEmailSent) {
            showSuccessMessage = true
            authViewModel.clearResetPasswordState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("忘记密码") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "重置密码",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "请输入您的电子邮箱，我们将向您发送重置密码的链接。",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { authViewModel.resetPassword(email) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotEmpty() && !authState.isLoading
                ) {
                    Text("发送重置链接")
                }
                
                // 显示成功消息
                if (showSuccessMessage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "重置密码的邮件已发送，请查看您的邮箱。",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 显示错误信息
                authState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorText(message = error)
                    LaunchedEffect(key1 = error) {
                        authViewModel.clearError()
                    }
                }
            }
            
            // 显示加载指示器
            if (authState.isLoading) {
                LoadingIndicator()
            }
        }
    }
} 