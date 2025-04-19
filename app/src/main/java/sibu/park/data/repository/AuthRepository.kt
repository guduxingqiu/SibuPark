package sibu.park.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import sibu.park.data.model.User
import sibu.park.data.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    
    // 检查用户是否已登录
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    // 当前用户ID，如果未登录返回null
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    // 登录
    suspend fun login(email: String, password: String): User {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("登录失败：认证成功但用户为空")
        
        return getUserData(firebaseUser)
    }
    
    // 注册新用户
    suspend fun register(
        username: String,
        email: String,
        phoneNumber: String,
        password: String,
        role: UserRole
    ): User {
        // 创建Firebase账户
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("注册失败：创建成功但用户为空")
            
        // 创建用户数据
        val user = User(
            id = firebaseUser.uid,
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            role = role
        )
        
        // 保存到Firestore
        usersCollection.document(firebaseUser.uid).set(user).await()
        
        return user
    }
    
    // 退出登录
    fun logout() {
        auth.signOut()
    }
    
    // 获取当前登录用户的数据
    suspend fun getCurrentUserData(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return getUserData(firebaseUser)
    }
    
    // 从Firestore获取用户详细信息
    private suspend fun getUserData(firebaseUser: FirebaseUser): User {
        val userDoc = usersCollection.document(firebaseUser.uid).get().await()
        
        return if (userDoc.exists()) {
            // 如果用户数据存在于Firestore
            userDoc.toObject(User::class.java) ?: createBasicUserProfile(firebaseUser)
        } else {
            // 如果用户数据不在Firestore，创建基本资料
            val user = createBasicUserProfile(firebaseUser)
            usersCollection.document(firebaseUser.uid).set(user).await()
            user
        }
    }
    
    // 创建基本用户资料
    private fun createBasicUserProfile(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            username = firebaseUser.displayName ?: "用户${firebaseUser.uid.takeLast(5)}",
            email = firebaseUser.email ?: "",
            phoneNumber = firebaseUser.phoneNumber ?: ""
        )
    }
    
    // 更新用户资料
    suspend fun updateUserProfile(
        username: String,
        email: String,
        phoneNumber: String
    ): User {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        // 获取当前用户数据
        val userDoc = usersCollection.document(userId).get().await()
        val currentUser = userDoc.toObject(User::class.java)
            ?: throw IllegalStateException("用户数据不存在")
        
        // 如果邮箱有变更，需要在Firebase Auth中更新
        if (email != currentUser.email && email.isNotEmpty()) {
            auth.currentUser?.updateEmail(email)?.await()
        }
        
        // 更新Firestore中的用户资料
        val updatedData = mapOf(
            "username" to username,
            "email" to email,
            "phoneNumber" to phoneNumber
        )
        
        usersCollection.document(userId).update(updatedData).await()
        
        // 返回更新后的用户对象
        return currentUser.copy(
            username = username,
            email = email,
            phoneNumber = phoneNumber
        )
    }
    
    // 重置密码
    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
    
    // 修改密码
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser
            ?: throw IllegalStateException("用户未登录")
        
        user.updatePassword(newPassword).await()
    }
} 