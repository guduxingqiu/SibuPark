package sibu.park.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sibu.park.models.Coupon
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.CouponViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckCouponScreen(
    onNavigateBack: () -> Unit,
    onNavigateToIssueFine: () -> Unit,
    couponViewModel: CouponViewModel
) {
    val couponState = couponViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var licensePlate by remember { mutableStateOf("") }
    var showInvalidDialog by remember { mutableStateOf(false) }
    var showValidDialog by remember { mutableStateOf(false) }
    
    // 检索到的优惠券
    val retrievedCoupon = couponState.value.currentCoupon
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("验证停车券") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 车牌输入区域
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "输入车牌号码进行验证",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("车牌号码") },
                            placeholder = { Text("请输入车牌号码") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    couponViewModel.checkCouponByLicensePlate(licensePlate)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = licensePlate.isNotBlank() && !couponState.value.isLoading
                        ) {
                            Text("验证")
                        }
                    }
                }
                
                // 验证结果
                if (retrievedCoupon != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val expiryDateStr = dateFormat.format(Date(retrievedCoupon.expiryDate.seconds * 1000))
                    val now = System.currentTimeMillis() / 1000
                    val isExpired = retrievedCoupon.expiryDate.seconds < now
                    val isValid = retrievedCoupon.remainingUses > 0 && !isExpired && retrievedCoupon.isActive
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isValid) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
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
                                    text = "验证结果",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isValid) 
                                        Color(0xFF4CAF50).copy(alpha = 0.2f) 
                                    else 
                                        Color(0xFFF44336).copy(alpha = 0.2f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = if (isValid) "有效" else "无效",
                                        color = if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CouponInfoRow(label = "优惠券ID", value = retrievedCoupon.id)
                            CouponInfoRow(label = "套餐名称", value = retrievedCoupon.packageName)
                            CouponInfoRow(label = "剩余使用次数", value = retrievedCoupon.remainingUses.toString())
                            CouponInfoRow(label = "有效期至", value = expiryDateStr)
                            CouponInfoRow(label = "用户ID", value = retrievedCoupon.userId)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isValid) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            couponViewModel.useCoupon(retrievedCoupon.id, licensePlate)
                                            showValidDialog = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Text("确认使用")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { showInvalidDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF44336)
                                        )
                                    ) {
                                        Text("拒绝通行")
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = onNavigateToIssueFine,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800)
                                        )
                                    ) {
                                        Text("开具罚单")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (couponState.value.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (couponState.value.error != null) {
                ErrorDialog(
                    errorMessage = couponState.value.error!!,
                    onDismiss = { couponViewModel.clearError() }
                )
            }
            
            // 有效优惠券对话框
            if (showValidDialog) {
                AlertDialog(
                    onDismissRequest = { showValidDialog = false },
                    title = { Text("优惠券已使用") },
                    text = { Text("已成功使用优惠券，车辆可以通行。") },
                    confirmButton = {
                        Button(
                            onClick = { 
                                showValidDialog = false
                                couponViewModel.clearCurrentCoupon()
                                licensePlate = ""
                            }
                        ) {
                            Text("确定")
                        }
                    }
                )
            }
            
            // 无效优惠券对话框
            if (showInvalidDialog) {
                AlertDialog(
                    onDismissRequest = { showInvalidDialog = false },
                    title = { Text("拒绝通行") },
                    text = { Text("此车辆无有效停车券，请拒绝通行或引导车主购买停车券。") },
                    confirmButton = {
                        Button(
                            onClick = { 
                                showInvalidDialog = false
                                couponViewModel.clearCurrentCoupon()
                                licensePlate = ""
                            }
                        ) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CouponInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
} 