package com.assesment.customer_service_app.products

import java.math.BigDecimal
import java.time.OffsetDateTime

data class Product(
    val id: Long? = null,
    val externalId: String?,
    val title: String,
    val price: BigDecimal?,
    val url: String?,
    val variantsJson: String? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
