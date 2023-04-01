package com.example.aux

interface UserRepository {
    fun getUserInfo(): User
    fun consumedProducts(): List<ConsumedProduct>
    fun addConsumedProduct(consumedProduct: ConsumedProduct)

}

data class User(
    val name: String,
    val goalCalories: Int?,
)

data class ConsumedProduct(
    val product: Product,
    val date: Long,
)