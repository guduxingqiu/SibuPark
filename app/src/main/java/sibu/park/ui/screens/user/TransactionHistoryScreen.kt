package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sibu.park.data.model.Transaction
import sibu.park.data.model.TransactionType
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    transactionViewModel: TransactionViewModel
) {
    val transactionState = transactionViewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        transactionViewModel.getUserTransactions()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易历史") },
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
                transactionState.value.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                transactionState.value.error != null -> {
                    ErrorDialog(
                        errorMessage = transactionState.value.error!!,
                        onDismiss = { transactionViewModel.clearError() }
                    )
                }
                transactionState.value.transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "您没有交易记录",
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
                                text = "交易历史",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(transactionState.value.transactions.sortedByDescending { it.timestamp }) { transaction ->
                            TransactionCard(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: Transaction
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val transactionDate = dateFormat.format(transaction.timestamp)
    
    val (backgroundColor, textColor) = when (transaction.type) {
        TransactionType.COUPON_PURCHASE -> Pair(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary
        )
        TransactionType.FINE_PAYMENT -> Pair(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.error
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
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
                    text = when (transaction.type) {
                        TransactionType.COUPON_PURCHASE -> "购买停车券"
                        TransactionType.FINE_PAYMENT -> "罚款支付"
                        else -> "其他交易"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = when (transaction.type) {
                        TransactionType.COUPON_PURCHASE -> "-¥${transaction.amount}"
                        TransactionType.FINE_PAYMENT -> "-¥${transaction.amount}"
                        else -> "¥${transaction.amount}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "交易日期: $transactionDate",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            transaction.description.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = "说明: $description",
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = "交易ID: ${transaction.id.take(8)}...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
} 