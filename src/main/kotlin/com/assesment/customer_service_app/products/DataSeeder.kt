package com.assesment.customer_service_app.products

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DataSeeder(private val repo: ProductRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun seedIfEmpty() {
        return try {
            val existing = repo.findAll(limit = 1)
            if (existing.isEmpty()) {
                repo.insertManual(
                    title = "Sample Product",
                    price = BigDecimal("19.99"),
                    url = "https://example.com/sample"
                )
                log.info("Seeded sample product as the database was empty")
            } else {
                log.debug("Skipping seed; products table already has data")
            }
        } catch (e: Exception) {
            // Don't fail the app if seeding encounters an error
            log.warn("Data seeding skipped due to error: ${e.message}")
        }
    }
}
