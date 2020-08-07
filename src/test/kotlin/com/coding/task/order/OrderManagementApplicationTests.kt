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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderManagementApplicationTests() {

	@Autowired
	private lateinit var orderService: OrderManagementService

    @Test
    fun placeOrderTest() {
		var items: ArrayList<String> = ArrayList<String>()
		items.addAll(arrayListOf("apple", "orange", "apple", "apple"))
		val response = orderService.placeOrder(items)
		if (response != null) {
			assertThat(response.message).containsIgnoringCase("Completed")
		}
    }
}