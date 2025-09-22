package com.assesment.customer_service_app.web

import com.assesment.customer_service_app.products.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
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
        model.addAttribute("products", repo.findAll(100))
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
        model.addAttribute("products", repo.findAll(100))
        return "fragments/products-table :: table"
    }
}
