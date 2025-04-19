package sibu.park.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import sibu.park.models.Coupon
import sibu.park.models.CouponPackage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val packagesCollection = firestore.collection("couponPackages")
    private val couponsCollection = firestore.collection("coupons")
    private val usageCollection = firestore.collection("couponUsages")
    private val usersCollection = firestore.collection("users")
    
    // 获取所有优惠券套餐
    suspend fun getCouponPackages(): List<CouponPackage> {
        val snapshot = packagesCollection
            .whereEqualTo("isActive", true)
            .orderBy("price")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(CouponPackage::class.java)?.copy(id = doc.id)
        }
    }
    
    // 获取用户拥有的优惠券
    suspend fun getUserCoupons(): List<Coupon> {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val snapshot = couponsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Coupon::class.java)?.copy(id = doc.id)
        }
    }
    
    // 购买优惠券
    suspend fun purchaseCoupon(packageId: String): Coupon {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        // 获取套餐信息
        val packageDoc = packagesCollection.document(packageId).get().await()
        val couponPackage = packageDoc.toObject(CouponPackage::class.java)
            ?: throw IllegalStateException("优惠券套餐不存在")
        
        // 创建优惠券
        val currentTime = Timestamp.now()
        val expirySeconds = currentTime.seconds + (couponPackage.validityDays * 24 * 60 * 60)
        val expiryDate = Timestamp(expirySeconds, 0)
        
        val coupon = Coupon(
            id = "",
            userId = userId,
            packageId = packageId,
            packageName = couponPackage.name,
            remainingUses = couponPackage.usageCount,
            purchaseDate = currentTime,
            expiryDate = expiryDate,
            isActive = true
        )
        
        // 保存到 Firestore
        val docRef = couponsCollection.add(coupon).await()
        return coupon.copy(id = docRef.id)
    }
    
    // 根据ID获取优惠券
    suspend fun getCouponById(couponId: String): Coupon {
        val docSnapshot = couponsCollection.document(couponId).get().await()
        return docSnapshot.toObject(Coupon::class.java)?.copy(id = docSnapshot.id)
            ?: throw IllegalStateException("优惠券不存在")
    }
    
    // 根据车牌号查询最新的有效优惠券
    suspend fun getCouponByLicensePlate(licensePlate: String): Coupon {
        // 1. 查找拥有此车牌的用户
        val userSnapshot = usersCollection
            .whereArrayContains("licensePlates", licensePlate)
            .limit(1)
            .get()
            .await()
        
        if (userSnapshot.isEmpty) {
            throw IllegalStateException("未找到与该车牌关联的用户")
        }
        
        val userId = userSnapshot.documents[0].id
        
        // 2. 查找该用户的有效优惠券
        val couponSnapshot = couponsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .whereGreaterThan("remainingUses", 0)
            .get()
            .await()
        
        if (couponSnapshot.isEmpty) {
            throw IllegalStateException("用户没有可用的优惠券")
        }
        
        // 返回第一个有效优惠券
        return couponSnapshot.documents[0].toObject(Coupon::class.java)?.copy(id = couponSnapshot.documents[0].id)
            ?: throw IllegalStateException("优惠券数据无效")
    }
    
    // 使用优惠券
    suspend fun useCoupon(couponId: String, licensePlate: String): Boolean {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        // 获取优惠券信息
        val couponDoc = couponsCollection.document(couponId).get().await()
        val coupon = couponDoc.toObject(Coupon::class.java)?.copy(id = couponId)
            ?: throw IllegalStateException("优惠券不存在")
        
        // 验证优惠券
        if (coupon.userId != userId) {
            throw IllegalStateException("无权使用此优惠券")
        }
        
        if (coupon.remainingUses <= 0) {
            throw IllegalStateException("优惠券已用完")
        }
        
        val now = Timestamp.now()
        if (coupon.expiryDate.seconds < now.seconds) {
            throw IllegalStateException("优惠券已过期")
        }
        
        if (!coupon.isActive) {
            throw IllegalStateException("优惠券已失效")
        }
        
        // 减少使用次数
        val updatedRemainingUses = coupon.remainingUses - 1
        
        // 记录使用记录
        val usage = hashMapOf(
            "couponId" to couponId,
            "userId" to userId,
            "licensePlate" to licensePlate,
            "usageTime" to now
        )
        
        // 事务操作，确保原子性
        firestore.runTransaction { transaction ->
            // 更新优惠券
            transaction.update(
                couponsCollection.document(couponId),
                "remainingUses", updatedRemainingUses
            )
            
            // 如果用完了，设置为非激活
            if (updatedRemainingUses <= 0) {
                transaction.update(
                    couponsCollection.document(couponId),
                    "isActive", false
                )
            }
            
            // 添加使用记录
            val usageRef = usageCollection.document()
            transaction.set(usageRef, usage)
        }.await()
        
        return true
    }
    
    // 检查优惠券信息
    suspend fun checkCoupon(couponId: String): Coupon? {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("用户未登录")
        
        val couponDoc = couponsCollection.document(couponId).get().await()
        val coupon = couponDoc.toObject(Coupon::class.java)?.copy(id = couponId)
        
        // 检查是否是用户的优惠券
        return if (coupon != null && coupon.userId == userId) {
            coupon
        } else {
            null
        }
    }
} 