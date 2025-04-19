package sibu.park.ui.screens.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import sibu.park.ui.components.LoadingIndicator
import sibu.park.ui.viewmodel.AuthViewModel
import sibu.park.ui.components.ErrorDialog

data class StaffMenuOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCheckCoupon: () -> Unit,
    onNavigateToIssueFine: () -> Unit,
    onNavigateToManageReport: () -> Unit,
    onNavigateToCarTracker: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.state.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(key1 = Unit) {
        authViewModel.getCurrentUser()
    }
    
    if (authState.isLoading) {
        LoadingIndicator()
        return
    }
    
    val currentUser = authState.user
    
    if (currentUser == null) {
        LaunchedEffect(key1 = Unit) {
            errorMessage = "用户未登录"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("西布停车管理系统") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "个人资料"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 欢迎信息
            Text(
                text = "欢迎，${currentUser?.username ?: "管理员"}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // 管理员功能菜单
            val menuOptions = listOf(
                StaffMenuOption(
                    title = "验证停车券",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = onNavigateToCheckCoupon
                ),
                StaffMenuOption(
                    title = "处理罚款",
                    icon = Icons.Default.AttachMoney,
                    onClick = onNavigateToIssueFine
                ),
                StaffMenuOption(
                    title = "处理报告",
                    icon = Icons.Default.Report,
                    onClick = onNavigateToManageReport
                )
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(menuOptions) { menuOption ->
                    StaffMenuCard(
                        title = menuOption.title,
                        icon = menuOption.icon,
                        onClick = menuOption.onClick
                    )
                }
            }
        }
    }
    
    if (errorMessage != null) {
        ErrorDialog(
            errorMessage = errorMessage!!,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
fun StaffMenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
} 