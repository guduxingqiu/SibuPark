package sibu.park.models

import com.google.firebase.Timestamp

enum class ReportType {
    ISSUE, FEEDBACK, SUGGESTION
}

enum class ReportStatus {
    SUBMITTED, IN_REVIEW, RESOLVED, CLOSED
}

data class Report(
    val id: String = "",
    val userId: String = "",
    val type: ReportType = ReportType.ISSUE,
    val title: String = "",
    val description: String = "",
    val parkingArea: String? = null,
    val parkingSpot: String? = null,
    val attachmentUrls: List<String> = listOf(),
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val submittedAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null,
    val staffNotes: String = ""
) 