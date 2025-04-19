package sibu.park.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import sibu.park.models.Report
import sibu.park.models.ReportStatus
import sibu.park.models.ReportType
import sibu.park.utils.Resource
import java.util.UUID

class ReportRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection("reports")

    fun getUserReports(): Flow<Resource<List<Report>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = reportsCollection
                .whereEqualTo("userId", userId)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val reports = snapshot.toObjects(Report::class.java)
            emit(Resource.Success(reports))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun submitReport(
        type: ReportType,
        title: String,
        description: String,
        parkingArea: String? = null,
        parkingSpot: String? = null,
        attachmentUrls: List<String> = listOf()
    ): Resource<Report> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            val reportId = UUID.randomUUID().toString()
            val report = Report(
                id = reportId,
                userId = userId,
                type = type,
                title = title,
                description = description,
                parkingArea = parkingArea,
                parkingSpot = parkingSpot,
                attachmentUrls = attachmentUrls,
                status = ReportStatus.SUBMITTED,
                submittedAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            reportsCollection.document(reportId).set(report).await()
            Resource.Success(report)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getReportDetails(reportId: String): Resource<Report> {
        return try {
            val reportDoc = reportsCollection.document(reportId).get().await()
            val report = reportDoc.toObject(Report::class.java)
                ?: return Resource.Error("Report not found")
            
            Resource.Success(report)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    // 工作人员使用的方法
    suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        staffNotes: String? = null
    ): Resource<Report> {
        val staffId = auth.currentUser?.uid ?: return Resource.Error("Staff not logged in")
        
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to Timestamp.now()
            )
            
            if (status == ReportStatus.RESOLVED) {
                updates["resolvedAt"] = Timestamp.now()
            }
            
            staffNotes?.let { updates["staffNotes"] = it }
            
            reportsCollection.document(reportId).update(updates).await()
            
            // 获取更新后的报告
            val updatedDoc = reportsCollection.document(reportId).get().await()
            val updatedReport = updatedDoc.toObject(Report::class.java)
                ?: return Resource.Error("Report not found")
            
            Resource.Success(updatedReport)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
} 