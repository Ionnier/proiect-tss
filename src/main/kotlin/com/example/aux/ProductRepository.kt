package com.example.aux

interface ProductRepository {
    fun getProducts(): List<Product>
}

data class Product(
    val name: String,
    val calories: Int,
    val saltContent: Int,
    val grams: Int,
)