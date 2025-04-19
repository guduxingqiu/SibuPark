package sibu.park.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sibu.park.data.model.CouponPackage
import sibu.park.ui.components.EmptyListText
import sibu.park.ui.components.ErrorDialog
import sibu.park.ui.components.LoadingIndicator
import sibu.park.ui.viewmodel.CouponViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyCouponScreen(
    onNavigateBack: () -> Unit,
    couponViewModel: CouponViewModel = viewModel()
) {
    val couponState by couponViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 加载优惠券套餐
    LaunchedEffect(key1 = Unit) {
        couponViewModel.getCouponPackages()
    }

    // 监听购买成功
    LaunchedEffect(key1 = couponState.isPurchaseSuccess) {
        if (couponState.isPurchaseSuccess) {
            snackbarHostState.showSnackbar("购买成功")
            couponViewModel.clearPurchaseSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("购买停车券") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (couponState.isLoading) {
                LoadingIndicator()
            } else if (couponState.couponPackages.isEmpty()) {
                EmptyListText(message = "暂无可用的停车券套餐")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(couponState.couponPackages) { packageItem ->
                        CouponPackageCard(
                            couponPackage = packageItem,
                            onPurchase = { couponViewModel.purchaseCoupon(packageItem.id) }
                        )
                    }
                }
            }

            // 显示错误信息
            couponState.error?.let { error ->
                ErrorDialog(
                    errorMessage = error,
                    onDismiss = { couponViewModel.clearError() }
                )
            }
        }
    }
}

@Composable
fun CouponPackageCard(
    couponPackage: CouponPackage,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = couponPackage.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = couponPackage.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "¥${couponPackage.price}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        couponPackage.originalPrice?.let { originalPrice ->
                            if (originalPrice > couponPackage.price) {
                                Text(
                                    text = " ¥${originalPrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${couponPackage.validityDays}天有效期",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(onClick = onPurchase) {
                    Text("购买")
                }
            }
        }
    }
}