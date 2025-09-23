package com.assesment.customer_service_app.web

import com.assesment.customer_service_app.product.infrastructure.ProductRepository
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

    @GetMapping(value = ["/products", "/products/"])
    fun products(
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "20") size: Int,
        @RequestParam(name = "sort", required = false, defaultValue = "id") sort: String,
        @RequestParam(name = "dir", required = false, defaultValue = "desc") dir: String,
        model: Model
    ): String {
        val safeSize = size.coerceIn(1, 100)
        val safePage = if (page < 0) 0 else page
        val offset = safePage * safeSize
        val list = repo.findPaged(offset = offset, size = safeSize, sort = sort, dir = dir)
        model.addAttribute("products", list)
        model.addAttribute("page", safePage)
        model.addAttribute("size", safeSize)
        model.addAttribute("sort", sort)
        model.addAttribute("dir", dir)
        model.addAttribute("hasPrev", safePage > 0)
        model.addAttribute("hasNext", list.size >= safeSize)
        return "fragments/products-table :: table"
    }

    @GetMapping(value = ["/search", "/search/"])
    fun searchPage(model: Model): String {
        model.addAttribute("appName", "Customer Service App")
        // Start with empty results; HTMX will populate as user types
        model.addAttribute("products", emptyList<Any>())
        return "search"
    }

    @GetMapping(value = ["/products/search", "/products/search/"])
    fun searchProducts(
        @RequestParam(name = "q", required = false, defaultValue = "") q: String,
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "20") size: Int,
        @RequestParam(name = "sort", required = false, defaultValue = "id") sort: String,
        @RequestParam(name = "dir", required = false, defaultValue = "desc") dir: String,
        model: Model
    ): String {
        val query = q.trim()
        val safeSize = size.coerceIn(1, 100)
        val safePage = if (page < 0) 0 else page
        val offset = safePage * safeSize
        val results = if (query.isBlank()) emptyList() else repo.searchByTitlePaged(query, offset, safeSize, sort, dir)
        model.addAttribute("products", results)
        model.addAttribute("page", safePage)
        model.addAttribute("size", safeSize)
        model.addAttribute("sort", sort)
        model.addAttribute("dir", dir)
        model.addAttribute("hasPrev", safePage > 0)
        model.addAttribute("hasNext", results.size >= safeSize)
        model.addAttribute("q", query)
        return "fragments/products-table :: table"
    }

    @PostMapping(value = ["/products/add", "/products/add/"])
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) price: String?,
        @RequestParam(required = false) url: String?,
        model: Model
    ): String {
        val priceValue: BigDecimal? = price?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { BigDecimal(it) }.getOrNull() }
        repo.insertManual(title = title.trim(), price = priceValue, url = url?.trim()?.takeIf { it.isNotEmpty() })
        val page = 0
        val size = 20
        val sort = "id"
        val dir = "desc"
        val list = repo.findPaged(offset = page * size, size = size, sort = sort, dir = dir)
        model.addAttribute("products", list)
        model.addAttribute("page", page)
        model.addAttribute("size", size)
        model.addAttribute("sort", sort)
        model.addAttribute("dir", dir)
        model.addAttribute("hasPrev", false)
        model.addAttribute("hasNext", list.size >= size)
        return "fragments/products-table :: table"
    }

    @GetMapping(value = ["/products/{id}/edit", "/products/{id}/edit/"])
    fun editProductPage(@PathVariable id: Long, model: Model): String {
        val product = repo.findById(id) ?: run {
            // If not found, redirect to home
            return "redirect:/"
        }
        model.addAttribute("product", product)
        model.addAttribute("appName", "Customer Service App")
        return "product-edit"
    }

    @PostMapping(value = ["/products/{id}/edit", "/products/{id}/edit/"])
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

    @PostMapping(value = ["/products/{id}/delete", "/products/{id}/delete/"])
    fun deleteProduct(@PathVariable id: Long, model: Model): String {
        repo.deleteById(id)
        val page = 0
        val size = 20
        val sort = "id"
        val dir = "desc"
        val list = repo.findPaged(offset = page * size, size = size, sort = sort, dir = dir)
        model.addAttribute("products", list)
        model.addAttribute("page", page)
        model.addAttribute("size", size)
        model.addAttribute("sort", sort)
        model.addAttribute("dir", dir)
        model.addAttribute("hasPrev", false)
        model.addAttribute("hasNext", list.size >= size)
        return "fragments/products-table :: table"
    }
}
