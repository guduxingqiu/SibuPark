package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import sibu.park.data.model.Coupon
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.CouponViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseCouponScreen(
    onNavigateBack: () -> Unit,
    couponViewModel: CouponViewModel
) {
    val couponState = couponViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showUseCouponDialog by remember { mutableStateOf(false) }
    var selectedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var licensePlate by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        couponViewModel.getUserCoupons()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用停车券") },
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
                couponState.value.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                couponState.value.error != null -> {
                    ErrorDialog(
                        errorMessage = couponState.value.error!!,
                        onDismiss = { couponViewModel.clearError() }
                    )
                }
                couponState.value.userCoupons.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "您没有可用的停车券",
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
                                text = "我的停车券",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(couponState.value.userCoupons) { coupon ->
                            CouponCard(
                                coupon = coupon,
                                onUseCoupon = {
                                    selectedCoupon = coupon
                                    showUseCouponDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showUseCouponDialog && selectedCoupon != null) {
        AlertDialog(
            onDismissRequest = { showUseCouponDialog = false },
            title = { Text("使用停车券") },
            text = {
                Column {
                    Text("请输入您的车牌号码以使用停车券:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("车牌号码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (licensePlate.isNotBlank()) {
                            scope.launch {
                                couponViewModel.useCoupon(selectedCoupon!!.id, licensePlate)
                                showUseCouponDialog = false
                                licensePlate = ""
                            }
                        }
                    },
                    enabled = licensePlate.isNotBlank()
                ) {
                    Text("确认使用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUseCouponDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponCard(
    coupon: Coupon,
    onUseCoupon: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val expiryDate = coupon.expiryDate?.let { dateFormat.format(it) } ?: "永久有效"
    val isUsed = coupon.remainingUses <= 0 || (coupon.expiryDate != null && coupon.expiryDate.seconds < System.currentTimeMillis() / 1000)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = coupon.packageName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isUsed) "已使用" else "未使用",
                    color = if (isUsed) Color.Gray else Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "有效期至: $expiryDate")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "剩余使用次数: ${coupon.remainingUses}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onUseCoupon,
                modifier = Modifier.align(Alignment.End),
                enabled = !isUsed && coupon.isActive
            ) {
                Text("使用停车券")
            }
        }
    }
} 