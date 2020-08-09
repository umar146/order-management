package com.coding.task.order

import com.coding.task.order.beans.OrderResponseBean
import com.coding.task.order.service.OrderManagementService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OrderManagementApplicationTests() {

	@Autowired
	private lateinit var orderService: OrderManagementService

	/**
	 * Unit testing for the positive scenario - placing the order with valid request.
	 */
    @Test
    fun testOrderServiceTest1() {
		var items: ArrayList<String> = ArrayList<String>()
		items.addAll(arrayListOf("apple", "orange", "apple", "apple"))
		val response = orderService.placeOrder(items)
		if (response != null) {
			assertThat(response.status).containsIgnoringCase("Completed")
		}
    }

	/**
	 * Unit testing for the negative scenario - placing the order with invalid request - no items requested.
	 */
	@Test
	fun testOrderServiceTest2() {
		var items: ArrayList<String> = ArrayList<String>()
		items.addAll(arrayListOf(""))
		val response = orderService.placeOrder(items)
		if (response != null) {
			assertThat(response.status).containsIgnoringCase("Failed")
		}
	}

	/**
	 * Unit testing for the negative scenario - placing the order & when the quantity is not available.
	 * Here for orange - available quantity is 1, we have requested for 2
	 */
	@Test
	fun testOrderServiceTest3() {
		var items: ArrayList<String> = ArrayList<String>()
		items.addAll(arrayListOf("apple", "orange", "apple", "orange"))
		val response = orderService.placeOrder(items)
		if (response != null) {
			assertThat(response.status).containsIgnoringCase("Failed")
		}
	}

	/**
	 * Unit testing for the negative scenario - placing the order for an item which is not available in the store.
	 * Here - Water melon is not available in the store
	 */
	@Test
	fun testOrderServiceTest4() {
		var items: ArrayList<String> = ArrayList<String>()
		items.addAll(arrayListOf("apple", "orange", "apple", "watermelon"))
		val response = orderService.placeOrder(items)
		if (response != null) {
			assertThat(response.status).containsIgnoringCase("Failed")
		}
	}
}