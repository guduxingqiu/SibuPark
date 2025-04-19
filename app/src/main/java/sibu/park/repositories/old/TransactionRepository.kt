package sibu.park.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import sibu.park.models.Transaction
import sibu.park.models.TransactionStatus
import sibu.park.models.TransactionType
import sibu.park.utils.Resource
import java.util.UUID

class TransactionRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val transactionsCollection = firestore.collection("transactions")

    fun getUserTransactions(): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = transactionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = snapshot.toObjects(Transaction::class.java)
            emit(Resource.Success(transactions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun createTransaction(
        type: TransactionType,
        amount: Double,
        referenceId: String,
        paymentMethod: String,
        notes: String = ""
    ): Resource<Transaction> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            val transactionId = UUID.randomUUID().toString()
            val transaction = Transaction(
                id = transactionId,
                userId = userId,
                type = type,
                amount = amount,
                referenceId = referenceId,
                paymentMethod = paymentMethod,
                status = TransactionStatus.PENDING,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                notes = notes
            )
            
            transactionsCollection.document(transactionId).set(transaction).await()
            Resource.Success(transaction)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus,
        paymentTransactionId: String = ""
    ): Resource<Transaction> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to Timestamp.now()
            )
            
            if (paymentTransactionId.isNotEmpty()) {
                updates["transactionId"] = paymentTransactionId
            }
            
            transactionsCollection.document(transactionId).update(updates).await()
            
            // 获取更新后的交易
            val updatedDoc = transactionsCollection.document(transactionId).get().await()
            val updatedTransaction = updatedDoc.toObject(Transaction::class.java)
                ?: return Resource.Error("Transaction not found")
            
            Resource.Success(updatedTransaction)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getTransactionById(transactionId: String): Resource<Transaction> {
        return try {
            val transactionDoc = transactionsCollection.document(transactionId).get().await()
            val transaction = transactionDoc.toObject(Transaction::class.java)
                ?: return Resource.Error("Transaction not found")
            
            Resource.Success(transaction)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
} 