package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.components.LoadingIndicator
import sibu.park.ui.viewmodel.AuthViewModel

data class MenuOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToBuyCoupon: () -> Unit,
    onNavigateToUseCoupon: () -> Unit,
    onNavigateToPayFine: () -> Unit,
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToSubmitReport: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("西布停车") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "用户资料")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (authState.isLoading) {
                LoadingIndicator()
            } else {
                val menuOptions = listOf(
                    MenuOption(
                        title = "购买停车券",
                        icon = Icons.Default.ShoppingCart,
                        onClick = onNavigateToBuyCoupon
                    ),
                    MenuOption(
                        title = "使用停车券",
                        icon = Icons.Default.ConfirmationNumber,
                        onClick = onNavigateToUseCoupon
                    ),
                    MenuOption(
                        title = "支付罚款",
                        icon = Icons.Default.Warning,
                        onClick = onNavigateToPayFine
                    ),
                    MenuOption(
                        title = "交易历史",
                        icon = Icons.Default.Receipt,
                        onClick = onNavigateToTransactionHistory
                    ),
                    MenuOption(
                        title = "提交报告",
                        icon = Icons.Default.Report,
                        onClick = onNavigateToSubmitReport
                    )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 欢迎信息
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "欢迎, ${authState.user?.username ?: "用户"}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "感谢使用西布停车场管理系统",
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // 菜单选项网格
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(menuOptions) { option ->
                            MenuCard(
                                title = option.title,
                                icon = option.icon,
                                onClick = option.onClick
                            )
                        }
                    }
                }
            }
            
            // 错误处理
            authState.error?.let { errorMessage ->
                ErrorDialog(
                    errorMessage = errorMessage,
                    onDismiss = { authViewModel.clearError() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 