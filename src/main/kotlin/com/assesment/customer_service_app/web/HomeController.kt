package com.assesment.customer_service_app.web

import com.assesment.customer_service_app.products.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@Controller
class HomeController(private val repo: ProductRepository) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("appName", "Customer Service App")
        return "index"
    }

    @GetMapping("/products")
    fun products(model: Model): String {
        model.addAttribute("products", repo.findAll(50))
        return "fragments/products-table :: table"
    }

    @GetMapping("/search")
    fun searchPage(model: Model): String {
        model.addAttribute("appName", "Customer Service App")
        // Start with empty results; HTMX will populate as user types
        model.addAttribute("products", emptyList<Any>())
        return "search"
    }

    @GetMapping("/products/search")
    fun searchProducts(@RequestParam(name = "q", required = false, defaultValue = "") q: String, model: Model): String {
        val query = q.trim()
        val results = if (query.isBlank()) emptyList() else repo.searchByTitle(query, 50)
        model.addAttribute("products", results)
        return "fragments/products-table :: table"
    }

    @PostMapping("/products/add")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) url: String?,
        model: Model
    ): String {
        val priceValue: BigDecimal? = price?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { BigDecimal(it) }.getOrNull() }
        repo.insertManual(title = title.trim(), price = priceValue, url = url?.trim()?.takeIf { it.isNotEmpty() })
        model.addAttribute("products", repo.findAll(50))
        return "fragments/products-table :: table"
    }

    @GetMapping("/products/{id}/edit")
    fun editProductPage(@PathVariable id: Long, model: Model): String {
        val product = repo.findById(id) ?: run {
            // If not found, redirect to home
            return "redirect:/"
        }
        model.addAttribute("product", product)
        model.addAttribute("appName", "Customer Service App")
        return "product-edit"
    }

    @PostMapping("/products/{id}/edit")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) url: String?,
    ): String {
        val cleanTitle = title.trim().ifEmpty { "Untitled" }
        val priceValue: BigDecimal? = price?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { BigDecimal(it) }.getOrNull() }
        val cleanUrl = url?.trim()?.takeIf { it.isNotEmpty() }
        repo.updateProduct(id, cleanTitle, priceValue, cleanUrl)
        return "redirect:/"
    }
}
