package sibu.park.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import sibu.park.data.model.ParkingRecord
import sibu.park.data.model.Transaction
import sibu.park.data.model.TransactionType
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val transactionsCollection = firestore.collection("transactions")
    private val parkingRecordsCollection = firestore.collection("parking_records")
    
    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        description: String,
        relatedItemId: String? = null
    ): Transaction {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val transaction = Transaction(
            id = "",
            userId = userId,
            amount = amount,
            type = type,
            description = description,
            timestamp = Date(),
            relatedItemId = relatedItemId
        )
        
        val docRef = transactionsCollection.add(transaction).await()
        return transaction.copy(id = docRef.id)
    }
    
    suspend fun getUserTransactions(): List<Transaction> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val snapshot = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)?.copy(id = doc.id)
        }
    }
    
    // 停车记录相关方法
    
    suspend fun addParkingRecord(
        licensePlate: String,
        parkingArea: String,
        parkingSpot: String,
        couponId: String? = null
    ): ParkingRecord {
        val userId = auth.currentUser?.uid
        
        val parkingRecord = ParkingRecord(
            id = "",
            licensePlate = licensePlate,
            parkingArea = parkingArea,
            parkingSpot = parkingSpot,
            userId = userId,
            couponId = couponId,
            entryTime = Date(),
            exitTime = null
        )
        
        val docRef = parkingRecordsCollection.add(parkingRecord).await()
        return parkingRecord.copy(id = docRef.id)
    }
    
    suspend fun updateParkingRecordExit(recordId: String): ParkingRecord {
        val recordDoc = parkingRecordsCollection.document(recordId).get().await()
        val record = recordDoc.toObject(ParkingRecord::class.java)?.copy(id = recordDoc.id)
            ?: throw IllegalArgumentException("停车记录不存在")
        
        // 更新出场时间
        parkingRecordsCollection.document(recordId).update("exitTime", Date()).await()
        
        return record.copy(exitTime = Date())
    }
    
    suspend fun getParkingRecordByLicensePlate(licensePlate: String): ParkingRecord? {
        val snapshot = parkingRecordsCollection
            .whereEqualTo("licensePlate", licensePlate)
            .whereEqualTo("exitTime", null) // 仅查找未出场的记录
            .get()
            .await()
        
        return if (snapshot.documents.isNotEmpty()) {
            val doc = snapshot.documents[0]
            doc.toObject(ParkingRecord::class.java)?.copy(id = doc.id)
        } else {
            null
        }
    }
    
    suspend fun getAllParkingRecords(): List<ParkingRecord> {
        val snapshot = parkingRecordsCollection
            .orderBy("entryTime")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            val record = doc.toObject(ParkingRecord::class.java)
            record?.copy(id = doc.id)
        }
    }
    
    suspend fun getActiveParkingRecords(): List<ParkingRecord> {
        val snapshot = parkingRecordsCollection
            .whereEqualTo("exitTime", null)
            .orderBy("entryTime")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            val record = doc.toObject(ParkingRecord::class.java)
            record?.copy(id = doc.id)
        }
    }
} 