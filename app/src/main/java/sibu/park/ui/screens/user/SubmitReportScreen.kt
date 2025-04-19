package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sibu.park.data.model.ReportType
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReportScreen(
    onNavigateBack: () -> Unit,
    reportViewModel: ReportViewModel
) {
    val reportState = reportViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedReportType by remember { mutableStateOf(ReportType.ISSUE) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // 监听提交成功
    LaunchedEffect(key1 = reportState.value.isSubmitSuccess) {
        if (reportState.value.isSubmitSuccess) {
            showSuccessMessage = true
            reportViewModel.clearSubmitSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提交报告") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "提交问题报告",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // 报告类型选择
                Text(
                    text = "报告类型",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // 报告类型下拉菜单
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    RadioGroup(
                        selectedOption = selectedReportType,
                        onOptionSelected = { selectedReportType = it }
                    )
                }
                
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("请输入简短的标题") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // 位置输入
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("位置") },
                    placeholder = { Text("请输入问题发生的位置") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // 详细描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("详细描述") },
                    placeholder = { Text("请详细描述您遇到的问题...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
                
                // 提交按钮
                Button(
                    onClick = {
                        scope.launch {
                            reportViewModel.submitReport(
                                type = selectedReportType,
                                title = title,
                                description = description,
                                parkingArea = location,
                                parkingSpot = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && description.isNotBlank() && !reportState.value.isLoading
                ) {
                    if (reportState.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("提交报告")
                    }
                }
            }
            
            if (reportState.value.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (reportState.value.error != null) {
                ErrorDialog(
                    errorMessage = reportState.value.error!!,
                    onDismiss = { reportViewModel.clearError() }
                )
            }
            
            if (showSuccessMessage) {
                AlertDialog(
                    onDismissRequest = { 
                        showSuccessMessage = false
                        onNavigateBack()
                    },
                    title = { Text("提交成功") },
                    text = { Text("您的报告已成功提交，我们将尽快处理。") },
                    confirmButton = {
                        Button(
                            onClick = { 
                                showSuccessMessage = false
                                onNavigateBack()
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

@Composable
fun RadioGroup(
    selectedOption: ReportType,
    onOptionSelected: (ReportType) -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        ReportType.values().forEach { reportType ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == reportType,
                    onClick = { onOptionSelected(reportType) }
                )
                Text(
                    text = when (reportType) {
                        ReportType.ISSUE -> "设施问题"
                        ReportType.FEEDBACK -> "支付问题"
                        ReportType.SUGGESTION -> "安全隐患"
                        else -> "其他问题"
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 