package com.example.impl

import com.example.aux.ConsumedProduct
import com.example.aux.ProductRepository
import com.example.aux.UserRepository
import org.joda.time.DateTime
import org.joda.time.Instant

const val EMPTY_PRODUCT_NAME = "com.example.aux.Product name can't be blank"
const val NO_GOAL = "A goal has not been set"
const val INVALID_GOAL = "Invalid goal calories"
const val PRODUCT_NAME_DOES_NOT_CONFIRM = "A product can only contain letter and spaces"
const val PRODUCT_DOES_NOT_EXIST = "com.example.aux.Product does not exist"
const val MULTIPLE_MATCHES_FOUND = "Multiple products with this name exist"
const val INVALID_SCHEDULED_DATE = "Invalid scheduled date"
const val EXCEEDS_GOAL = "Exceeds goal calories"
const val TOO_MUCH_SALT = "Too much salt. Drink some water."


/**
 * @param productName The name of the product the user wishes to introduce
 * @param productRepository The Product repository which allows product search
 * @param userRepository The User Repository which allows retrieving information about the user
 * @param dateTime The time and date as timestamp at which the user wishes to schedule the product
 * @param force If the product should ignore a possible caloric surplus.
 * @return a nullable error message if an error occurs
 */
fun canSchedule(
    productName: String,
    productRepository: ProductRepository,
    userRepository: UserRepository,
    dateTime: Long,
    force: Boolean,
): String? {
    if (productName.isBlank()) {
        return EMPTY_PRODUCT_NAME
    }

    if (!productName.matches(Regex("^[a-zA-Z\\s]*\$"))) {
        return PRODUCT_NAME_DOES_NOT_CONFIRM
    }

    val goalCalories = userRepository.getUserInfo().goalCalories ?: return NO_GOAL

    if (goalCalories <= 0L) {
        return INVALID_GOAL
    }

    if (dateTime < DateTime.now().millis) {
        return INVALID_SCHEDULED_DATE
    }

    val possibleProducts = productRepository.getProducts()
        .filter { it.name.lowercase().indexOf(productName.lowercase().trim()) != -1 }
    when (possibleProducts.size) {
        0 -> return PRODUCT_DOES_NOT_EXIST
        1 -> 1 + 1
        else -> return MULTIPLE_MATCHES_FOUND
    }

    val product = possibleProducts.first()

    val date = DateTime(Instant(dateTime))

    if (force == false) {
        val consumedCalories = userRepository.consumedProducts().filter {
            (DateTime(Instant(it.date)).dayOfYear == date.dayOfYear) &&
                    DateTime(Instant(it.date)).year == date.year
        }.sumOf { it.product.calories }

        if (consumedCalories + product.calories > goalCalories) {
            return EXCEEDS_GOAL
        }

        val consumedSalt = userRepository.consumedProducts().filter {
            (DateTime(Instant(it.date)).weekOfWeekyear == date.weekOfWeekyear) &&
                    DateTime(Instant(it.date)).year == date.year
        }.sumOf { it.product.saltContent } + product.saltContent

        val consumedWater = userRepository.consumedProducts().filter {
            (DateTime(Instant(it.date)).weekOfWeekyear == date.weekOfWeekyear) &&
                    DateTime(Instant(it.date)).year == date.year && (it.product.name.lowercase().indexOf("water") != -1)
        }.sumOf { it.product.grams } + if (product.name.lowercase().indexOf("water") == -1) 0 else product.grams

        val dayOfWeek = date.dayOfWeek
        val idealSaltConsumptionPerDay = 2
        val idealWaterConsumptionPerDay = 2000

        if (consumedSalt > (idealSaltConsumptionPerDay * dayOfWeek * 2) && consumedWater < (idealWaterConsumptionPerDay * dayOfWeek * 2)) {
            return TOO_MUCH_SALT
        }

    }

    userRepository.addConsumedProduct(consumedProduct = ConsumedProduct(product, dateTime))
    return null

}