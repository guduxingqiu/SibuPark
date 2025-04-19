package sibu.park.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.viewmodel.ParkingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarTrackerScreen(
    onNavigateBack: () -> Unit,
    parkingViewModel: ParkingViewModel
) {
    val parkingState by parkingViewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(key1 = Unit) {
        parkingViewModel.getParkingStatus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("车辆追踪") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
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
                .padding(16.dp)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入车牌号搜索") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        parkingViewModel.findVehicle(searchQuery)
                        focusManager.clearFocus()
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 统计信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "当前车辆",
                    value = parkingState.parkingSpots.count { it.isOccupied }.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    title = "空闲车位",
                    value = parkingState.parkingSpots.count { !it.isOccupied }.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    title = "总车位",
                    value = parkingState.parkingSpots.size.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "车辆记录",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // 筛选按钮
                Row {
                    AssistChip(
                        onClick = { parkingViewModel.getParkingStatus() },
                        label = { Text("刷新") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 车辆列表
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    parkingState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    parkingState.parkingSpots.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无车辆记录")
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(parkingState.parkingSpots) { spot ->
                                ParkingSpotCard(spot = spot)
                            }
                        }
                    }
                }
                
                // 错误提示
                if (parkingState.error != null) {
                    ErrorDialog(
                        errorMessage = parkingState.error!!,
                        onDismiss = { parkingViewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.1f))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ParkingSpotCard(spot: sibu.park.ui.viewmodel.ParkingSpot) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (spot.isOccupied)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 车辆信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spot.licensePlate ?: "空车位",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${spot.area} - ${spot.number}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // 时间信息
            Column(horizontalAlignment = Alignment.End) {
                if (spot.isOccupied && spot.occupiedSince != null) {
                    Text(
                        text = "进入时间:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = dateFormat.format(spot.occupiedSince),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "可停车",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
} 