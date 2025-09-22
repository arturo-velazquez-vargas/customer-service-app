package com.assesment.customer_service_app.products

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Component
class ProductImportScheduler(
    private val repo: ProductRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rest = RestTemplate()

    @Scheduled(initialDelay = 0, fixedDelay = 12 * 60 * 60 * 1000) // every 12h, start immediately
    fun importProducts() {
        try {
            val url = "https://famme.no/products.json"
            val response = rest.getForObject(url, String::class.java) ?: return
            val wrapper: ProductsWrapper = objectMapper.readValue(response)
            val products = wrapper.products.take(50)
            var count = 0
            products.forEach { p ->
                val product = Product(
                    externalId = p.id?.toString(),
                    title = p.title ?: "Untitled",
                    price = p.variants?.firstOrNull()?.price?.toBigDecimalOrNull(),
                    url = p.handle?.let { "https://famme.no/products/$it" },
                    variantsJson = p.variants?.let { objectMapper.writeValueAsString(it) }
                )
                count += repo.upsertByExternalId(product)
            }
            log.info("Imported/updated {} products", count)
        } catch (e: Exception) {
            log.error("Failed to import products", e)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductsWrapper(val products: List<FammeProduct> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
data class FammeProduct(
    val id: Long?,
    val title: String?,
    val handle: String?,
    val variants: List<FammeVariant>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FammeVariant(
    val id: Long?,
    val title: String?,
    val price: String
)
