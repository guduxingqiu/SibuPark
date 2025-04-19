package sibu.park.repositories

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import sibu.park.models.User
import sibu.park.models.UserRole
import sibu.park.utils.Resource

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun register(
        username: String,
        email: String,
        phoneNumber: String,
        password: String,
        role: UserRole = UserRole.USER
    ): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Failed to get user ID")
            
            // 创建用户文档
            val user = User(
                id = userId,
                username = username,
                email = email,
                phoneNumber = phoneNumber,
                role = role
            )
            
            usersCollection.document(userId).set(user).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun login(email: String, password: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun resetPassword(email: String): Flow<Resource<Void>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUserData(): Resource<User> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java) ?: User()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun updateUserProfile(
        username: String? = null,
        email: String? = null,
        phoneNumber: String? = null
    ): Resource<Unit> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        return try {
            val updates = mutableMapOf<String, Any>()
            
            username?.let { updates["username"] = it }
            email?.let { 
                updates["email"] = it
                auth.currentUser?.updateEmail(it)?.await()
            }
            phoneNumber?.let { updates["phoneNumber"] = it }
            
            if (updates.isNotEmpty()) {
                usersCollection.document(userId).update(updates).await()
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun updatePassword(newPassword: String): Resource<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
} 