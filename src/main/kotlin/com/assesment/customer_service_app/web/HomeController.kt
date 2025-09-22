package com.assesment.customer_service_app.web

import com.assesment.customer_service_app.products.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

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
}
