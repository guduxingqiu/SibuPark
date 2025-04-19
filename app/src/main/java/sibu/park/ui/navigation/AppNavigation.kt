package sibu.park.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import sibu.park.ui.screens.auth.LoginScreen
import sibu.park.ui.screens.auth.RegisterScreen
import sibu.park.ui.screens.staff.*
import sibu.park.ui.screens.user.*
import sibu.park.ui.viewmodel.*
import sibu.park.ui.viewmodel.AuthViewModel
import sibu.park.ui.viewmodel.CouponViewModel
import sibu.park.ui.viewmodel.FineViewModel
import sibu.park.ui.viewmodel.ParkingViewModel
import sibu.park.ui.viewmodel.ReportViewModel
import sibu.park.ui.viewmodel.TransactionViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object UserHome : Screen("user_home")
    object AdminHome : Screen("admin_home")
    object UserProfile : Screen("user_profile")
    object BuyCoupon : Screen("buy_coupon")
    object UseCoupon : Screen("use_coupon")
    object PayFine : Screen("pay_fine")
    object TransactionHistory : Screen("transaction_history")
    object SubmitReport : Screen("submit_report")
    
    // Admin screens
    object StaffProfile : Screen("staff_profile")
    object CheckCoupon : Screen("check_coupon")
    object IssueFine : Screen("issue_fine")
    object ManageReport : Screen("manage_report")
    object CarTracker : Screen("car_tracker")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val actions = remember(navController) { NavActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(
                onNavigateToRegister = actions.navigateToRegister,
                onNavigateToForgotPassword = {},
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Register.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            RegisterScreen(
                onNavigateToLogin = actions.navigateBack,
                authViewModel = authViewModel
            )
        }
        
        // 用户相关屏幕
        composable(Screen.UserHome.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            UserHomeScreen(
                onNavigateToProfile = actions.navigateToUserProfile,
                onNavigateToBuyCoupon = actions.navigateToBuyCoupon,
                onNavigateToUseCoupon = actions.navigateToUseCoupon,
                onNavigateToPayFine = actions.navigateToPayFine,
                onNavigateToTransactionHistory = actions.navigateToTransactionHistory,
                onNavigateToSubmitReport = actions.navigateToSubmitReport,
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.UserProfile.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            UserProfileScreen(
                onNavigateBack = actions.navigateBack,
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.BuyCoupon.route) {
            val couponViewModel = hiltViewModel<CouponViewModel>()
            BuyCouponScreen(
                onNavigateBack = actions.navigateBack,
                couponViewModel = couponViewModel
            )
        }
        
        composable(Screen.UseCoupon.route) {
            val couponViewModel = hiltViewModel<CouponViewModel>()
            UseCouponScreen(
                onNavigateBack = actions.navigateBack,
                couponViewModel = couponViewModel
            )
        }
        
        composable(Screen.PayFine.route) {
            val fineViewModel = hiltViewModel<FineViewModel>()
            PayFineScreen(
                onNavigateBack = actions.navigateBack,
                fineViewModel = fineViewModel
            )
        }
        
        composable(Screen.TransactionHistory.route) {
            val transactionViewModel = hiltViewModel<TransactionViewModel>()
            TransactionHistoryScreen(
                onNavigateBack = actions.navigateBack,
                transactionViewModel = transactionViewModel
            )
        }
        
        composable(Screen.SubmitReport.route) {
            val reportViewModel = hiltViewModel<ReportViewModel>()
            SubmitReportScreen(
                onNavigateBack = actions.navigateBack,
                reportViewModel = reportViewModel
            )
        }
        
        // 管理员相关屏幕
        composable(Screen.AdminHome.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            StaffHomeScreen(
                onNavigateToProfile = actions.navigateToStaffProfile,
                onNavigateToCheckCoupon = actions.navigateToCheckCoupon,
                onNavigateToIssueFine = actions.navigateToIssueFine,
                onNavigateToManageReport = actions.navigateToManageReport,
                onNavigateToCarTracker = actions.navigateToCarTracker,
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.StaffProfile.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            StaffProfileScreen(
                onNavigateBack = actions.navigateBack,
                onLogout = {
                    authViewModel.logout()
                    actions.navigateToLogin()
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.CheckCoupon.route) {
            val couponViewModel = hiltViewModel<CouponViewModel>()
            CheckCouponScreen(
                onNavigateBack = actions.navigateBack,
                onNavigateToIssueFine = actions.navigateToIssueFine,
                couponViewModel = couponViewModel
            )
        }
        
        composable(Screen.IssueFine.route) {
            val fineViewModel = hiltViewModel<FineViewModel>()
            IssueFineScreen(
                onNavigateBack = actions.navigateBack,
                fineViewModel = fineViewModel
            )
        }
        
        composable(Screen.ManageReport.route) {
            val reportViewModel = hiltViewModel<ReportViewModel>()
            ManageReportScreen(
                onNavigateBack = actions.navigateBack,
                reportViewModel = reportViewModel
            )
        }
        
        composable(Screen.CarTracker.route) {
            val parkingViewModel = hiltViewModel<ParkingViewModel>()
            CarTrackerScreen(
                onNavigateBack = actions.navigateBack,
                parkingViewModel = parkingViewModel
            )
        }
    }
}

class NavActions(private val navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }
    
    val navigateToUserHome: () -> Unit = {
        navController.navigate(Screen.UserHome.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToAdminHome: () -> Unit = {
        navController.navigate(Screen.AdminHome.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    // 用户导航
    val navigateToUserProfile: () -> Unit = {
        navController.navigate(Screen.UserProfile.route)
    }
    
    val navigateToBuyCoupon: () -> Unit = {
        navController.navigate(Screen.BuyCoupon.route)
    }
    
    val navigateToUseCoupon: () -> Unit = {
        navController.navigate(Screen.UseCoupon.route)
    }
    
    val navigateToPayFine: () -> Unit = {
        navController.navigate(Screen.PayFine.route)
    }
    
    val navigateToTransactionHistory: () -> Unit = {
        navController.navigate(Screen.TransactionHistory.route)
    }
    
    val navigateToSubmitReport: () -> Unit = {
        navController.navigate(Screen.SubmitReport.route)
    }
    
    // 管理员导航
    val navigateToStaffProfile: () -> Unit = {
        navController.navigate(Screen.StaffProfile.route)
    }
    
    val navigateToCheckCoupon: () -> Unit = {
        navController.navigate(Screen.CheckCoupon.route)
    }
    
    val navigateToIssueFine: () -> Unit = {
        navController.navigate(Screen.IssueFine.route)
    }
    
    val navigateToManageReport: () -> Unit = {
        navController.navigate(Screen.ManageReport.route)
    }
    
    val navigateToCarTracker: () -> Unit = {
        navController.navigate(Screen.CarTracker.route)
    }
    
    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
} 