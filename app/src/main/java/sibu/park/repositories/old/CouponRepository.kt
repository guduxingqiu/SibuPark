package sibu.park.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import sibu.park.models.Coupon
import sibu.park.models.CouponPackage
import sibu.park.models.CouponUsage
import sibu.park.utils.Resource
import java.util.UUID

class CouponRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val packagesCollection = firestore.collection("couponPackages")
    private val couponsCollection = firestore.collection("coupons")
    private val usagesCollection = firestore.collection("couponUsages")

    fun getCouponPackages(): Flow<Resource<List<CouponPackage>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = packagesCollection.whereEqualTo("isActive", true).get().await()
            val packages = snapshot.toObjects(CouponPackage::class.java)
            emit(Resource.Success(packages))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun getUserCoupons(): Flow<Resource<List<Coupon>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = couponsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val coupons = snapshot.toObjects(Coupon::class.java)
            emit(Resource.Success(coupons))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun purchaseCoupon(packageId: String): Resource<Coupon> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            // 获取套餐信息
            val packageDoc = packagesCollection.document(packageId).get().await()
            val couponPackage = packageDoc.toObject(CouponPackage::class.java) 
                ?: return Resource.Error("Package not found")
            
            if (!couponPackage.isActive) {
                return Resource.Error("Package is no longer available")
            }
            
            // 计算过期时间
            val currentSeconds = Timestamp.now().seconds
            val expirySeconds = currentSeconds + (couponPackage.validityDays * 24 * 60 * 60)
            val expiryDate = Timestamp(expirySeconds, 0)
            
            // 创建优惠券
            val couponId = UUID.randomUUID().toString()
            val coupon = Coupon(
                id = couponId,
                userId = userId,
                packageId = packageId,
                packageName = couponPackage.name,
                remainingUses = couponPackage.usageCount,
                purchaseDate = Timestamp.now(),
                expiryDate = expiryDate,
                isActive = true
            )
            
            // 保存到Firestore
            couponsCollection.document(couponId).set(coupon).await()
            
            Resource.Success(coupon)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun useCoupon(
        couponId: String,
        parkingArea: String,
        parkingSpot: String,
        licensePlate: String
    ): Resource<CouponUsage> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            // 获取优惠券信息
            val couponDoc = couponsCollection.document(couponId).get().await()
            val coupon = couponDoc.toObject(Coupon::class.java) 
                ?: return Resource.Error("Coupon not found")
            
            // 检查优惠券是否有效
            if (!coupon.isActive) {
                return Resource.Error("Coupon is not active")
            }
            
            if (coupon.userId != userId) {
                return Resource.Error("This coupon does not belong to you")
            }
            
            if (coupon.remainingUses <= 0) {
                return Resource.Error("No remaining uses left on this coupon")
            }
            
            val now = Timestamp.now()
            if (now.seconds > coupon.expiryDate.seconds) {
                return Resource.Error("Coupon has expired")
            }
            
            // 创建使用记录
            val usageId = UUID.randomUUID().toString()
            val usage = CouponUsage(
                id = usageId,
                couponId = couponId,
                userId = userId,
                parkingArea = parkingArea,
                parkingSpot = parkingSpot,
                licensePlate = licensePlate,
                usageTime = now
            )
            
            // 减少剩余使用次数
            val updatedRemainingUses = coupon.remainingUses - 1
            val updates = mapOf(
                "remainingUses" to updatedRemainingUses,
                "isActive" to (updatedRemainingUses > 0)
            )
            
            // 保存使用记录和更新优惠券
            usagesCollection.document(usageId).set(usage).await()
            couponsCollection.document(couponId).update(updates).await()
            
            Resource.Success(usage)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun checkCouponUsageByLicensePlate(licensePlate: String): Resource<Boolean> {
        return try {
            val currentTime = Timestamp.now()
            // 查找过去2小时内的优惠券使用记录
            val twoHoursAgo = Timestamp(currentTime.seconds - (2 * 60 * 60), 0)
            
            val snapshot = usagesCollection
                .whereEqualTo("licensePlate", licensePlate)
                .whereGreaterThan("usageTime", twoHoursAgo)
                .get()
                .await()
            
            val hasValidUsage = !snapshot.isEmpty
            Resource.Success(hasValidUsage)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    fun getCouponUsageHistory(): Flow<Resource<List<CouponUsage>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = usagesCollection
                .whereEqualTo("userId", userId)
                .orderBy("usageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val usages = snapshot.toObjects(CouponUsage::class.java)
            emit(Resource.Success(usages))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun saveLicensePlate(licensePlate: String): Resource<Unit> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            // 获取用户文档
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            // 获取当前保存的车牌号
            @Suppress("UNCHECKED_CAST")
            val savedPlates = userDoc.get("savedPlates") as? List<String> ?: listOf()
            
            // 如果车牌号已存在，则不添加
            if (savedPlates.contains(licensePlate)) {
                return Resource.Success(Unit)
            }
            
            // 更新保存的车牌号列表
            val updatedPlates = savedPlates + licensePlate
            firestore.collection("users").document(userId)
                .update("savedPlates", updatedPlates)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getSavedLicensePlates(): Resource<List<String>> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        
        return try {
            // 获取用户文档
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            // 获取保存的车牌号
            @Suppress("UNCHECKED_CAST")
            val savedPlates = userDoc.get("savedPlates") as? List<String> ?: listOf()
            
            Resource.Success(savedPlates)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
} 