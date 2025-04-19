package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sibu.park.data.model.Fine
import sibu.park.data.model.FineStatus
import sibu.park.ui.viewmodel.FineViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FineDetailsScreen(
    fineId: String,
    onNavigateBack: () -> Unit,
    fineViewModel: FineViewModel? = null
) {
    var fine by remember { mutableStateOf<Fine?>(null) }
    
    // 这里应该从 ViewModel 获取罚款详情
    // 暂时使用模拟数据
    LaunchedEffect(fineId) {
        fine = Fine(
            id = fineId,
            licensePlate = "粤B12345",
            parkingArea = "A区",
            parkingSpot = "A-001",
            amount = 100.0,
            notes = "超时停车",
            issuedAt = Date(),
            status = FineStatus.PENDING
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("罚款详情") },
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
            if (fine == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                FineDetails(fine = fine!!)
            }
        }
    }
}

@Composable
private fun FineDetails(fine: Fine) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val issuedDate = dateFormat.format(fine.issuedAt)
    val paidDate = fine.paidAt?.let { dateFormat.format(it) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 罚款状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (fine.status) {
                    FineStatus.PENDING -> MaterialTheme.colorScheme.errorContainer
                    FineStatus.PAID -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = when (fine.status) {
                        FineStatus.PENDING -> "未支付"
                        FineStatus.PAID -> "已支付"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "罚款金额: ¥${fine.amount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (fine.status == FineStatus.PAID && paidDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "支付日期: $paidDate")
                }
            }
        }
        
        // 罚款详情
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "违规详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Divider()
                
                InfoRow(label = "违规原因", value = fine.notes)
                InfoRow(label = "开具日期", value = issuedDate)
                InfoRow(label = "车牌号码", value = fine.licensePlate)
                InfoRow(label = "停车区域", value = fine.parkingArea)
                InfoRow(label = "停车位置", value = fine.parkingSpot)
                InfoRow(label = "罚单编号", value = fine.id)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(text = value)
    }
} 