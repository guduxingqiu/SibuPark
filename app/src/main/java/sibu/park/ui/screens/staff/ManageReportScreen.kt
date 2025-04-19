package sibu.park.ui.screens.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sibu.park.data.model.Report
import sibu.park.data.model.ReportStatus
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.components.LoadingIndicator
import sibu.park.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageReportScreen(
    onNavigateBack: () -> Unit,
    reportViewModel: ReportViewModel
) {
    val reportState by reportViewModel.state.collectAsState()
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var showReportDetails by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = Unit) {
        reportViewModel.getAllReports()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("处理用户报告") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
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
                reportState.isLoading -> {
                    LoadingIndicator()
                }
                reportState.reports.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无报告")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportState.reports) { report ->
                            ReportCard(
                                report = report,
                                onClick = {
                                    selectedReport = report
                                    showReportDetails = true
                                }
                            )
                        }
                    }
                }
            }
            
            // 错误提示
            if (reportState.error != null) {
                ErrorDialog(
                    errorMessage = reportState.error!!,
                    onDismiss = { reportViewModel.clearError() }
                )
            }
        }
    }
    
    // 报告详情对话框
    if (showReportDetails && selectedReport != null) {
        ReportDetailsDialog(
            report = selectedReport!!,
            onDismiss = { showReportDetails = false },
            onUpdateStatus = { status ->
                reportViewModel.updateReportStatus(selectedReport!!.id, status)
                showReportDetails = false
            }
        )
    }
}

@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val submissionTimeStr = dateFormat.format(Date(report.submissionTime))
    
    val statusColors = mapOf(
        ReportStatus.SUBMITTED to MaterialTheme.colorScheme.tertiary,
        ReportStatus.PROCESSING to MaterialTheme.colorScheme.primary,
        ReportStatus.RESOLVED to MaterialTheme.colorScheme.secondary,
        ReportStatus.CLOSED to MaterialTheme.colorScheme.secondary,
        ReportStatus.REOPENED to MaterialTheme.colorScheme.error
    )
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(
                    containerColor = statusColors[report.status] ?: MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = when(report.status) {
                            ReportStatus.SUBMITTED -> "已提交"
                            ReportStatus.PROCESSING -> "处理中"
                            ReportStatus.RESOLVED -> "已解决"
                            ReportStatus.CLOSED -> "已关闭"
                            ReportStatus.REOPENED -> "已重开"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "提交者: ${report.userId}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = submissionTimeStr,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ReportDetailsDialog(
    report: Report,
    onDismiss: () -> Unit,
    onUpdateStatus: (ReportStatus) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val submissionTimeStr = dateFormat.format(Date(report.submissionTime))
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = report.title) },
        text = {
            Column {
                Text(
                    text = "状态：${
                        when(report.status) {
                            ReportStatus.SUBMITTED -> "已提交"
                            ReportStatus.PROCESSING -> "处理中"
                            ReportStatus.RESOLVED -> "已解决"
                            ReportStatus.CLOSED -> "已关闭"
                            ReportStatus.REOPENED -> "已重开"
                        }
                    }"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "提交者：${report.userId}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "提交时间：${submissionTimeStr}")
                Spacer(modifier = Modifier.height(8.dp))
                
                report.parkingAreaId?.let { 
                    Text(text = "停车场：${it}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                report.locationDescription?.let {
                    Text(text = "位置描述：${it}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "描述",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report.description)
                
                Spacer(modifier = Modifier.height(16.dp))
                if (report.status != ReportStatus.RESOLVED && report.status != ReportStatus.CLOSED) {
                    Text(
                        text = "更新状态",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (report.status == ReportStatus.SUBMITTED) {
                            Button(onClick = { onUpdateStatus(ReportStatus.PROCESSING) }) {
                                Text("开始处理")
                            }
                        }
                        if (report.status == ReportStatus.PROCESSING) {
                            Button(onClick = { onUpdateStatus(ReportStatus.RESOLVED) }) {
                                Text("标记为解决")
                            }
                        }
                        if (report.status == ReportStatus.SUBMITTED || report.status == ReportStatus.PROCESSING) {
                            OutlinedButton(onClick = { onUpdateStatus(ReportStatus.CLOSED) }) {
                                Text("关闭报告")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
} 