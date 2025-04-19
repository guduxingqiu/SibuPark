package sibu.park.ui.navigation

/**
 * 应用程序的导航路由
 */
object NavRoutes {
    // 认证路由
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // 用户路由
    const val USER_HOME = "user_home"
    const val BUY_COUPON = "buy_coupon"
    const val USE_COUPON = "use_coupon"
    const val PAY_FINE = "pay_fine"
    const val FINE_DETAILS = "fine_details/{fineId}"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val SUBMIT_REPORT = "submit_report"
    const val USER_PROFILE = "user_profile"
    
    // 工作人员路由
    const val STAFF_HOME = "staff_home"
    const val CHECK_COUPON = "check_coupon"
    const val ISSUE_FINE = "issue_fine"
    const val STAFF_PROFILE = "staff_profile"
    const val MANAGE_REPORT = "manage_report"
    const val CAR_TRACKER = "car_tracker"
    
    // 构建带参数的路由
    fun fineDetails(fineId: String) = "fine_details/$fineId"
} 