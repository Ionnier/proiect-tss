package com.example.impl

import com.example.aux.*
import junit.framework.TestCase.assertEquals
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.BeforeParam
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class ExampleUnitTest(
    val productName: String,
    val goalCalories: Int?,
    val consumedProduct: List<ConsumedProduct>,
    val products: List<Product>,
    val time: Long,
    val force: Boolean,
    val expected: String?
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<Any?>> {
            return listOf(
                arrayOf("", 2000, emptyList<ConsumedProduct>(), emptyList<Product>(), DateTime.now().millis, false, EMPTY_PRODUCT_NAME),
                arrayOf("    ", 2000, emptyList<ConsumedProduct>(), emptyList<Product>(), DateTime.now().millis, false, EMPTY_PRODUCT_NAME),
                arrayOf(";", 2000, emptyList<ConsumedProduct>(), emptyList<Product>(), DateTime.now().millis, false, PRODUCT_NAME_DOES_NOT_CONFIRM),
                arrayOf("a", 2000, emptyList<ConsumedProduct>(), emptyList<Product>(), 0, false, INVALID_SCHEDULED_DATE),
                arrayOf("a", null, emptyList<ConsumedProduct>(), emptyList<Product>(), 0, false, NO_GOAL),
                arrayOf("a", 0, emptyList<ConsumedProduct>(), emptyList<Product>(), 0, false, INVALID_GOAL),
                arrayOf("a", 1000, emptyList<ConsumedProduct>(), emptyList<Product>(), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, PRODUCT_DOES_NOT_EXIST),
                arrayOf("a", 1000, emptyList<ConsumedProduct>(), listOf(Product("a", 0, 0,0), Product("ab", 0, 0,0)), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, MULTIPLE_MATCHES_FOUND),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("a", calories = 123123123, saltContent = 123, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1))), listOf(
                    Product("a", 0, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), true, null),
                arrayOf("a", 1000, emptyList<ConsumedProduct>(), listOf(Product("a", 0, 0,0)), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, null),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("b", calories = 500, saltContent = 123, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1))), listOf(
                    Product("a", 0, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, TOO_MUCH_SALT
                ),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("b", calories = 900, saltContent = 0, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1))), listOf(
                    Product("a", 0, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, null),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("b", calories = 900, saltContent = 0, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1))), listOf(
                    Product("a", 200, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, EXCEEDS_GOAL
                ),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("b", calories = 900, saltContent = 0, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1))), listOf(
                    Product("a", 100, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, null
                ),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("b", calories = 900, saltContent = 0, grams = 12312), DateTime.now().millis + TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1))), listOf(
                    Product("a", 0, 0,0)
                ), DateTime.now().millis + TimeUnit.DAYS.toMillis(1), false, null),
                arrayOf("a", 1000, listOf(ConsumedProduct(Product("a", calories = 34543, saltContent = 0, grams = 100),  DateTime(Instant(DateTime.now().millis - TimeUnit.DAYS.toMillis(365))).millis)), listOf(
                    Product("a", 2, 0,0)
                ), DateTime.now().millis + TimeUnit.MINUTES.toMillis(1), false, null),
            )
        }
    }

    @Test
    fun testSchedule() {
        val userRepositoryImpl = object: UserRepository {
            override fun getUserInfo(): User {
                return User("", goalCalories)
            }

            override fun consumedProducts(): List<ConsumedProduct> {
                return consumedProduct
            }

            override fun addConsumedProduct(consumedProduct: ConsumedProduct) {

            }

        }

        val productRepositoryImpl = object: ProductRepository {
            override fun getProducts(): List<Product> {
                return products
            }
        }

        val date = DateTime(Instant(DateTime.now().millis + TimeUnit.MINUTES.toMillis(1)))
        val date2 = DateTime(Instant(DateTime.now().millis - TimeUnit.DAYS.toMillis(365)))
        print("${date.dayOfYear} ${date.year} ${date2.dayOfYear} ${date2.year}")
        assertEquals(
            expected,
            canSchedule(
                productName,
                productRepositoryImpl,
                userRepositoryImpl,
                time,
                force
            )
        )
    }
}