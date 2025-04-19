package sibu.park.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.FineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueFineScreen(
    onNavigateBack: () -> Unit,
    fineViewModel: FineViewModel
) {
    val fineState = fineViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var licensePlate by remember { mutableStateOf("") }
    var parkingArea by remember { mutableStateOf("") }
    var parkingSpot by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("50") } // 默认罚款50元
    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // 监听罚单开具成功
    LaunchedEffect(fineState.value.isFineIssued) {
        if (fineState.value.isFineIssued) {
            showSuccessDialog = true
            fineViewModel.clearFineIssued()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开具罚单") },
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
                // 罚单信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "开具停车违规罚单",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // 罚单表单
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("车牌号码") },
                    placeholder = { Text("请输入违规车辆的车牌号码") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = parkingArea,
                        onValueChange = { parkingArea = it },
                        label = { Text("停车区域") },
                        placeholder = { Text("如: A区") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = parkingSpot,
                        onValueChange = { parkingSpot = it },
                        label = { Text("车位号") },
                        placeholder = { Text("如: A-123") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            amount = it 
                        }
                    },
                    label = { Text("罚款金额 (元)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    prefix = { Text("¥") }
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("违规原因") },
                    placeholder = { Text("请详细描述违规情况...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            val amountValue = amount.toDoubleOrNull() ?: 50.0
                            fineViewModel.issueFine(
                                licensePlate = licensePlate,
                                parkingArea = parkingArea,
                                parkingSpot = parkingSpot,
                                amount = amountValue,
                                notes = notes
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = licensePlate.isNotBlank() && notes.isNotBlank() && !fineState.value.isLoading
                ) {
                    Text("开具罚单")
                }
            }
            
            if (fineState.value.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (fineState.value.error != null) {
                ErrorDialog(
                    errorMessage = fineState.value.error!!,
                    onDismiss = { fineViewModel.clearError() }
                )
            }
            
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showSuccessDialog = false
                        // 清空表单
                        licensePlate = ""
                        parkingArea = ""
                        parkingSpot = ""
                        amount = "50"
                        notes = "" 
                    },
                    title = { Text("罚单已开具") },
                    text = { Text("罚单已成功开具并通知车主。") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                // 清空表单
                                licensePlate = ""
                                parkingArea = ""
                                parkingSpot = ""
                                amount = "50"
                                notes = ""
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
} 