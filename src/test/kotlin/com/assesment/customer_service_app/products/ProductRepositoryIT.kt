package com.assesment.customer_service_app.products

import com.assesment.customer_service_app.product.infrastructure.ProductRepository
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = [ProductRepositoryIT.Initializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class ProductRepositoryIT @Autowired constructor(
    private val repo: ProductRepository,
    private val flyway: Flyway
) {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16")
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(context: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=${postgres.jdbcUrl}",
                "spring.datasource.username=${postgres.username}",
                "spring.datasource.password=${postgres.password}",
            ).applyTo(context.environment)
        }
    }

    @Test
    fun `migrates and can insert and list`() {
        flyway.migrate()
        repo.insertManual(title = "Test Product", price = BigDecimal("9.99"), url = "https://example.com")
        val list = repo.findAll(10)
        assertTrue(list.isNotEmpty())
    }
}
