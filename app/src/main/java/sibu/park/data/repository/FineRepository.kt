package sibu.park.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import sibu.park.data.model.Fine
import sibu.park.data.model.FineStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FineRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val finesCollection = firestore.collection("fines")
    
    // 获取用户罚款列表
    suspend fun getUserFines(): List<Fine> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val snapshot = finesCollection
            .whereEqualTo("userId", userId)
            .orderBy("issuedAt")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Fine::class.java)?.copy(id = doc.id)
        }
    }
    
    // 获取未支付的罚款
    suspend fun getUnpaidFines(): List<Fine> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val snapshot = finesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", FineStatus.PENDING)
            .orderBy("issuedAt")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Fine::class.java)?.copy(id = doc.id)
        }
    }
    
    // 获取罚款详情
    suspend fun getFineById(fineId: String): Fine? {
        val fineDoc = finesCollection.document(fineId).get().await()
        
        return if (fineDoc.exists()) {
            fineDoc.toObject(Fine::class.java)?.copy(id = fineId)
        } else {
            null
        }
    }
    
    // 支付罚款
    suspend fun payFine(fineId: String, transactionId: String? = null): Boolean {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        // 获取罚款信息
        val fineDoc = finesCollection.document(fineId).get().await()
        val fine = fineDoc.toObject(Fine::class.java)?.copy(id = fineId)
            ?: throw IllegalStateException("罚款不存在")
        
        // 验证罚款所有权
        if (fine.userId != userId) {
            throw IllegalStateException("无权支付此罚款")
        }
        
        // 如果已支付，返回成功
        if (fine.status == FineStatus.PAID) {
            return true
        }
        
        // 更新罚款状态
        val updates = mutableMapOf<String, Any>(
            "status" to FineStatus.PAID,
            "paidAt" to Date(),
            "updatedAt" to Date()
        )
        
        // 如果有交易ID，也更新
        if (transactionId != null) {
            updates["transactionId"] = transactionId
        }
        
        finesCollection.document(fineId).update(updates).await()
        return true
    }
    
    // 发布罚款（工作人员使用）
    suspend fun issueFine(
        licensePlate: String,
        parkingArea: String,
        parkingSpot: String?,
        amount: Double,
        notes: String?
    ): Fine {
        val staffId = auth.currentUser?.uid
            ?: throw IllegalStateException("工作人员未登录")
        
        val fine = Fine(
            id = "",
            userId = "", // 需要根据车牌号查找用户ID
            licensePlate = licensePlate,
            parkingArea = parkingArea,
            parkingSpot = parkingSpot ?: "",
            amount = amount,
            notes = notes ?: "",
            issuedAt = Date(),
            status = FineStatus.PENDING
        )
        
        val docRef = finesCollection.add(fine).await()
        return fine.copy(id = docRef.id)
    }
    
    // 根据车牌查询罚款
    suspend fun getFinesByLicensePlate(licensePlate: String): List<Fine> {
        val snapshot = finesCollection
            .whereEqualTo("licensePlate", licensePlate)
            .orderBy("issuedAt")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Fine::class.java)?.copy(id = doc.id)
        }
    }
} 