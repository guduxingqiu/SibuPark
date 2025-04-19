package sibu.park.data.model

import java.util.Date

enum class ReportStatus {
    SUBMITTED,   // 已提交
    PROCESSING,  // 处理中
    RESOLVED,    // 已解决
    CLOSED,      // 已关闭
    REOPENED     // 重新打开
}

enum class ReportType {
    FACILITY_ISSUE,  // 设施问题
    SERVICE_COMPLAINT, // 服务投诉
    PAYMENT_ISSUE,   // 支付问题
    APP_BUG,         // 应用程序错误
    SUGGESTION,      // 建议
    OTHER            // 其他
}

data class Report(
    val id: String = "",
    val userId: String = "",        // 报告提交用户ID
    val title: String = "",         // 报告标题
    val type: ReportType = ReportType.OTHER,
    val description: String = "",   // 详细描述
    val parkingAreaId: String? = null, // 相关停车场ID（如适用）
    val locationDescription: String? = null, // 位置描述
    val imageUrls: List<String> = emptyList(), // 图片URL列表
    val videoUrl: String? = null,   // 视频URL（如适用）
    val submissionTime: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val assignedToStaffId: String? = null, // 分配给的工作人员ID
    val priority: Int = 3,          // 优先级 1-5，5为最高
    val responseMessages: List<ReportResponse> = emptyList(),
    val resolvedTime: Long? = null, // 解决时间
    val satisfactionRating: Int? = null, // 满意度评分 1-5
    val feedbackComment: String? = null,  // 反馈意见
    val contactPhone: String? = null,  // 联系电话
    val updatedTime: Long = System.currentTimeMillis() // 最后更新时间
)

data class ReportResponse(
    val id: String = "",
    val reportId: String = "",      // 对应的报告ID
    val staffId: String? = null,    // 回复的工作人员ID
    val userId: String? = null,     // 回复的用户ID（如果是用户回复）
    val message: String = "",       // 回复内容
    val time: Long = System.currentTimeMillis(),
    val attachmentUrls: List<String> = emptyList() // 附件URL
) 