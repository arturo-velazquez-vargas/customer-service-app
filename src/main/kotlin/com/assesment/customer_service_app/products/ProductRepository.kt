package com.assesment.customer_service_app.product.infrastructure

import com.assesment.customer_service_app.product.domain.Product
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductRepository(private val jdbc: JdbcClient) {

    fun findAll(limit: Int = 100): List<Product> =
        jdbc.sql(
            """
            select id, external_id, title, price, url, variants, created_at, updated_at
            from products
            order by id desc
            limit :limit
            """.trimIndent()
        )
            .param("limit", limit)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    externalId = rs.getString("external_id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    url = rs.getString("url"),
                    variantsJson = rs.getString("variants"),
                    createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java),
                )
            }
            .list()

    fun findPaged(offset: Int, size: Int, sort: String, dir: String): List<Product> {
        val column = when (sort.lowercase()) {
            "id", "title", "price", "created_at", "updated_at" -> sort.lowercase()
            else -> "id"
        }
        val direction = if (dir.equals("asc", true)) "asc" else "desc"
        val sql = """
            select id, external_id, title, price, url, variants, created_at, updated_at
            from products
            order by $column $direction, id desc
            limit :limit offset :offset
        """.trimIndent()
        return jdbc.sql(sql)
            .param("limit", size)
            .param("offset", offset)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    externalId = rs.getString("external_id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    url = rs.getString("url"),
                    variantsJson = rs.getString("variants"),
                    createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java),
                )
            }
            .list()
    }

    fun findById(id: Long): Product? =
        jdbc.sql(
            """
            select id, external_id, title, price, url, variants, created_at, updated_at
            from products
            where id = :id
            """.trimIndent()
        )
            .param("id", id)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    externalId = rs.getString("external_id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    url = rs.getString("url"),
                    variantsJson = rs.getString("variants"),
                    createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java),
                )
            }
            .list()
            .firstOrNull()

    fun searchByTitle(query: String, limit: Int = 50): List<Product> =
        jdbc.sql(
            """
            select id, external_id, title, price, url, variants, created_at, updated_at
            from products
            where title ilike concat('%', :q, '%')
            order by id desc
            limit :limit
            """.trimIndent()
        )
            .param("q", query)
            .param("limit", limit)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    externalId = rs.getString("external_id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    url = rs.getString("url"),
                    variantsJson = rs.getString("variants"),
                    createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java),
                )
            }
            .list()

    fun searchByTitlePaged(query: String, offset: Int, size: Int, sort: String, dir: String): List<Product> {
        val column = when (sort.lowercase()) {
            "id", "title", "price", "created_at", "updated_at" -> sort.lowercase()
            else -> "id"
        }
        val direction = if (dir.equals("asc", true)) "asc" else "desc"
        val sql = """
            select id, external_id, title, price, url, variants, created_at, updated_at
            from products
            where title ilike concat('%', :q, '%')
            order by $column $direction, id desc
            limit :limit offset :offset
        """.trimIndent()
        return jdbc.sql(sql)
            .param("q", query)
            .param("limit", size)
            .param("offset", offset)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    externalId = rs.getString("external_id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    url = rs.getString("url"),
                    variantsJson = rs.getString("variants"),
                    createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java),
                )
            }
            .list()
    }

    fun upsertByExternalId(p: Product): Int =
        jdbc.sql(
            """
            insert into products(external_id, title, price, url, variants)
            values (:externalId, :title, :price, :url, cast(:variantsJson as jsonb))
            on conflict (external_id) do update
            set title = excluded.title,
                price = excluded.price,
                url = excluded.url,
                variants = excluded.variants,
                updated_at = now()
            """.trimIndent()
        )
            .param("externalId", p.externalId)
            .param("title", p.title)
            .param("price", p.price)
            .param("url", p.url)
            .param("variantsJson", p.variantsJson)
            .update()

    fun updateProduct(id: Long, title: String, price: BigDecimal?, url: String?): Int =
        jdbc.sql(
            """
            update products
            set title = :title,
                price = :price,
                url = :url,
                updated_at = now()
            where id = :id
            """.trimIndent()
        )
            .param("id", id)
            .param("title", title)
            .param("price", price)
            .param("url", url)
            .update()

    fun insertManual(title: String, price: BigDecimal?, url: String?): Long? =
        jdbc.sql(
            """
            insert into products(title, price, url)
            values (:title, :price, :url)
            returning id
            """.trimIndent()
        )
            .param("title", title)
            .param("price", price)
            .param("url", url)
            .query(Long::class.java)
            .single()

    fun deleteById(id: Long): Int =
        jdbc.sql("delete from products where id = :id")
            .param("id", id)
            .update()
}
