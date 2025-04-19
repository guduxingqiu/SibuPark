package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sibu.park.data.model.Fine
import sibu.park.data.model.FineStatus
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.FineViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayFineScreen(
    onNavigateBack: () -> Unit,
    fineViewModel: FineViewModel
) {
    val fineState = fineViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedFine by remember { mutableStateOf<Fine?>(null) }
    
    LaunchedEffect(Unit) {
        fineViewModel.getUserFines()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("支付罚款") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            when {
                fineState.value.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                fineState.value.error != null -> {
                    ErrorDialog(
                        errorMessage = fineState.value.error!!,
                        onDismiss = { fineViewModel.clearError() }
                    )
                }
                fineState.value.fines.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "您没有未付罚款",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateBack) {
                            Text("返回")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "我的罚款",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        val unpaidFines = fineState.value.fines.filter { it.status == FineStatus.PENDING }
                        items(unpaidFines) { fine ->
                            FineCard(
                                fine = fine,
                                onPayFine = {
                                    selectedFine = fine
                                    showPaymentDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showPaymentDialog && selectedFine != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("支付罚款") },
            text = {
                Column {
                    Text("您确定要支付以下罚款吗？")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "违规原因: ${selectedFine!!.notes}",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "罚款金额: ¥${selectedFine!!.amount}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "车牌号码: ${selectedFine!!.licensePlate}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            fineViewModel.payFine(selectedFine!!.id)
                            showPaymentDialog = false
                        }
                    }
                ) {
                    Text("确认支付")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FineCard(
    fine: Fine,
    onPayFine: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val issuedDate = dateFormat.format(fine.issuedAt)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "罚款通知",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "¥${fine.amount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "发布日期: $issuedDate",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "违规原因: ${fine.notes}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "车牌号码: ${fine.licensePlate}",
                fontSize = 14.sp
            )
            
            Text(
                text = "位置: ${fine.parkingArea} ${fine.parkingSpot}",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onPayFine,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("支付罚款")
            }
        }
    }
} 