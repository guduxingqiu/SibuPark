package sibu.park.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import sibu.park.data.model.UserRole
import sibu.park.ui.screens.auth.ForgotPasswordScreen
import sibu.park.ui.screens.auth.LoginScreen
import sibu.park.ui.screens.auth.RegisterScreen
import sibu.park.ui.screens.staff.CheckCouponScreen
import sibu.park.ui.screens.staff.IssueFineScreen
import sibu.park.ui.screens.staff.StaffHomeScreen
import sibu.park.ui.screens.staff.StaffProfileScreen
import sibu.park.ui.screens.user.BuyCouponScreen
import sibu.park.ui.screens.user.PayFineScreen
import sibu.park.ui.screens.user.SubmitReportScreen
import sibu.park.ui.screens.user.TransactionHistoryScreen
import sibu.park.ui.screens.user.UseCouponScreen
import sibu.park.ui.screens.user.UserHomeScreen
import sibu.park.ui.screens.user.UserProfileScreen
import sibu.park.ui.viewmodel.AuthViewModel
import sibu.park.ui.viewmodel.CouponViewModel
import sibu.park.ui.viewmodel.FineViewModel
import sibu.park.ui.viewmodel.ReportViewModel
import sibu.park.ui.viewmodel.TransactionViewModel

@Composable
fun Navigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val couponViewModel: CouponViewModel = hiltViewModel()
    val fineViewModel: FineViewModel = hiltViewModel()
    val transactionViewModel: TransactionViewModel = hiltViewModel()
    val reportViewModel: ReportViewModel = hiltViewModel()
    
    val authState by authViewModel.state.collectAsState()
    
    // 根据登录状态自动跳转
    LaunchedEffect(key1 = authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            // 根据用户角色跳转到不同的主页
            val homeRoute = if (authState.user?.role == UserRole.STAFF) {
                NavRoutes.STAFF_HOME
            } else {
                NavRoutes.USER_HOME
            }
            navController.navigate(homeRoute) {
                // 清除认证相关的返回栈
                popUpTo(NavRoutes.LOGIN) { inclusive = true }
            }
        } else if (navController.currentDestination?.route != NavRoutes.LOGIN &&
                navController.currentDestination?.route != NavRoutes.REGISTER &&
                navController.currentDestination?.route != NavRoutes.FORGOT_PASSWORD) {
            // 如果未登录且不在认证页面，则跳转到登录页
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
        // 认证相关路由
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
                authViewModel = authViewModel
            )
        }
        
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        
        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        
        // 用户相关路由
        composable(NavRoutes.USER_HOME) {
            UserHomeScreen(
                onNavigateToBuyCoupon = { navController.navigate(NavRoutes.BUY_COUPON) },
                onNavigateToUseCoupon = { navController.navigate(NavRoutes.USE_COUPON) },
                onNavigateToPayFine = { navController.navigate(NavRoutes.PAY_FINE) },
                onNavigateToTransactionHistory = { navController.navigate(NavRoutes.TRANSACTION_HISTORY) },
                onNavigateToSubmitReport = { navController.navigate(NavRoutes.SUBMIT_REPORT) },
                onNavigateToProfile = { navController.navigate(NavRoutes.USER_PROFILE) },
                authViewModel = authViewModel
            )
        }
        
        composable(NavRoutes.BUY_COUPON) {
            BuyCouponScreen(
                onNavigateBack = { navController.popBackStack() },
                couponViewModel = couponViewModel
            )
        }
        
        composable(NavRoutes.USE_COUPON) {
            UseCouponScreen(
                onNavigateBack = { navController.popBackStack() },
                couponViewModel = couponViewModel
            )
        }
        
        composable(NavRoutes.PAY_FINE) {
            PayFineScreen(
                onNavigateBack = { navController.popBackStack() },
                fineViewModel = fineViewModel
            )
        }
        
        composable(
            NavRoutes.FINE_DETAILS,
            arguments = listOf(navArgument("fineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fineId = backStackEntry.arguments?.getString("fineId") ?: ""
            sibu.park.ui.screens.user.FineDetailsScreen(
                fineId = fineId,
                onNavigateBack = { navController.popBackStack() },
                fineViewModel = fineViewModel
            )
        }
        
        composable(NavRoutes.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                transactionViewModel = transactionViewModel
            )
        }
        
        composable(NavRoutes.SUBMIT_REPORT) {
            SubmitReportScreen(
                onNavigateBack = { navController.popBackStack() },
                reportViewModel = reportViewModel
            )
        }
        
        composable(NavRoutes.USER_PROFILE) {
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                },
                authViewModel = authViewModel
            )
        }
        
        // 工作人员相关路由
        composable(NavRoutes.STAFF_HOME) {
            StaffHomeScreen(
                onNavigateToCheckCoupon = { navController.navigate(NavRoutes.CHECK_COUPON) },
                onNavigateToIssueFine = { navController.navigate(NavRoutes.ISSUE_FINE) },
                onNavigateToManageReport = { navController.navigate(NavRoutes.MANAGE_REPORT) },
                onNavigateToCarTracker = { navController.navigate(NavRoutes.CAR_TRACKER) },
                onNavigateToProfile = { navController.navigate(NavRoutes.STAFF_PROFILE) },
                authViewModel = authViewModel
            )
        }
        
        composable(NavRoutes.CHECK_COUPON) {
            CheckCouponScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToIssueFine = { navController.navigate(NavRoutes.ISSUE_FINE) },
                couponViewModel = couponViewModel
            )
        }
        
        composable(NavRoutes.ISSUE_FINE) {
            IssueFineScreen(
                onNavigateBack = { navController.popBackStack() },
                fineViewModel = fineViewModel
            )
        }
        
        composable(NavRoutes.STAFF_PROFILE) {
            StaffProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                },
                authViewModel = authViewModel
            )
        }
    }
} 