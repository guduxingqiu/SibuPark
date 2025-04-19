package sibu.park.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import sibu.park.data.model.Report
import sibu.park.data.model.ReportStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val reportsCollection = firestore.collection("reports")
    
    suspend fun submitReport(
        title: String,
        description: String,
        parkingArea: String,
        parkingSpot: String? = null
    ): Report {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val report = Report(
            id = "",
            userId = userId,
            title = title,
            description = description,
            parkingArea = parkingArea,
            parkingSpot = parkingSpot,
            createdAt = Date(),
            status = ReportStatus.PENDING
        )
        
        val docRef = reportsCollection.add(report).await()
        return report.copy(id = docRef.id)
    }
    
    suspend fun getUserReports(): List<Report> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val snapshot = reportsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            val report = doc.toObject(Report::class.java)
            report?.copy(id = doc.id)
        }
    }
    
    suspend fun getAllReports(): List<Report> {
        val snapshot = reportsCollection
            .orderBy("createdAt")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            val report = doc.toObject(Report::class.java)
            report?.copy(id = doc.id)
        }
    }
    
    suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus) {
        reportsCollection.document(reportId)
            .update("status", newStatus)
            .await()
    }
} 