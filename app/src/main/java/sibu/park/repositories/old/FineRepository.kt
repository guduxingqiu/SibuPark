package sibu.park.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import sibu.park.models.Fine
import sibu.park.models.FinePayment
import sibu.park.models.FineStatus
import sibu.park.utils.Resource
import java.util.UUID

class FineRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val finesCollection = firestore.collection("fines")
    private val paymentsCollection = firestore.collection("finePayments")

    fun getUserFines(): Flow<Resource<List<Fine>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = finesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val fines = snapshot.toObjects(Fine::class.java)
            emit(Resource.Success(fines))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun issueFine(
        userId: String,
        licensePlate: String,
        parkingArea: String,
        parkingSpot: String,
        amount: Double,
        notes: String
    ): Resource<Fine> {
        val staffId = auth.currentUser?.uid ?: return Resource.Error("Staff not logged in")
        
        return try {
            val fineId = UUID.randomUUID().toString()
            val fine = Fine(
                id = fineId,
                userId = userId,
                licensePlate = licensePlate,
                parkingArea = parkingArea,
                parkingSpot = parkingSpot,
                amount = amount,
                issuedBy = staffId,
                issuedAt = Timestamp.now(),
                status = FineStatus.PENDING,
                notes = notes
            )
            
            finesCollection.document(fineId).set(fine).await()
            Resource.Success(fine)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun payFine(
        fineId: String,
        amount: Double,
        paymentMethod: String,
        transactionId: String
    ): Resource<FinePayment> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            // 获取罚款信息
            val fineDoc = finesCollection.document(fineId).get().await()
            val fine = fineDoc.toObject(Fine::class.java) 
                ?: return Resource.Error("Fine not found")
            
            // 检查罚款状态
            if (fine.status != FineStatus.PENDING) {
                return Resource.Error("Fine is already ${fine.status}")
            }
            
            if (fine.userId != userId) {
                return Resource.Error("This fine does not belong to you")
            }
            
            // 检查金额是否正确
            if (amount < fine.amount) {
                return Resource.Error("Payment amount is less than the fine amount")
            }
            
            // 创建支付记录
            val paymentId = UUID.randomUUID().toString()
            val payment = FinePayment(
                id = paymentId,
                fineId = fineId,
                userId = userId,
                amount = amount,
                paymentMethod = paymentMethod,
                transactionId = transactionId,
                paymentDate = Timestamp.now()
            )
            
            // 更新罚款状态
            val updates = mapOf(
                "status" to FineStatus.PAID,
                "paidAt" to Timestamp.now()
            )
            
            // 保存支付记录和更新罚款
            paymentsCollection.document(paymentId).set(payment).await()
            finesCollection.document(fineId).update(updates).await()
            
            Resource.Success(payment)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getFinesByLicensePlate(licensePlate: String): Resource<List<Fine>> {
        return try {
            val snapshot = finesCollection
                .whereEqualTo("licensePlate", licensePlate)
                .whereEqualTo("status", FineStatus.PENDING)
                .get()
                .await()
            
            val fines = snapshot.toObjects(Fine::class.java)
            Resource.Success(fines)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getFineDetails(fineId: String): Resource<Fine> {
        return try {
            val fineDoc = finesCollection.document(fineId).get().await()
            val fine = fineDoc.toObject(Fine::class.java) 
                ?: return Resource.Error("Fine not found")
            
            Resource.Success(fine)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    fun getUnpaidFines(): Flow<Resource<List<Fine>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = finesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", FineStatus.PENDING)
                .get()
                .await()
            
            val fines = snapshot.toObjects(Fine::class.java)
            emit(Resource.Success(fines))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 