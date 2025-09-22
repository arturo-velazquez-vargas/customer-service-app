package com.assesment.customer_service_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CustomerServiceAppApplication

fun main(args: Array<String>) {
    runApplication<CustomerServiceAppApplication>(*args)
}
