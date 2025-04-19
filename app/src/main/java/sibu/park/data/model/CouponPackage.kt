package sibu.park.data.model

data class CouponPackage(
    val id: String = "",
    val name: String = "",            // 套餐名称，如"黄金会员套餐"
    val description: String = "",     // 详细描述
    val price: Double = 0.0,          // 套餐价格
    val originalPrice: Double? = null, // 原价，用于显示折扣
    val coupons: List<CouponTemplate> = emptyList(), // 包含的优惠券模板
    val imageUrl: String? = null,     // 套餐图片
    val validityDays: Int = 30,       // 有效期（天）
    val isActive: Boolean = true,     // 是否上架销售
    val creationTime: Long = System.currentTimeMillis(),
    val salesCount: Int = 0,          // 销售数量
    val tags: List<String> = emptyList(), // 标签，如"热门"，"限时"
    val sortOrder: Int = 0            // 排序顺序
)

data class CouponTemplate(
    val type: CouponType = CouponType.TIME_BASED,
    val value: Double = 0.0,
    val description: String = "",
    val validityDays: Int = 30,
    val restrictions: List<String> = emptyList()
) 