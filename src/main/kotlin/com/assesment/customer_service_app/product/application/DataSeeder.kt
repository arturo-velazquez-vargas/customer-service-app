package com.assesment.customer_service_app.product.application

import com.assesment.customer_service_app.product.infrastructure.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import java.math.BigDecimal

@Component
class DataSeeder(private val repo: ProductRepository) {
    private val log = LoggerFactory.getLogger(DataSeeder::class.java)

    @PostConstruct
    fun seedIfEmpty() {
        try {
            val existing = repo.findAll(1)
            if (existing.isEmpty()) {
                repo.insertManual(
                    title = "Sample Product",
                    price = BigDecimal("9.99"),
                    url = "https://example.com/sample"
                )
                log.info("DataSeeder: inserted sample product because table was empty")
            } else {
                log.info("DataSeeder: products table already has data (count>=1), skipping seeding")
            }
        } catch (e: Exception) {
            // Do not fail app startup if DB is temporarily unavailable
            log.warn("DataSeeder: skipping seeding due to error: ${e.message}")
        }
    }
}
