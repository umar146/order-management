package com.coding.task.order

import com.coding.task.order.beans.OrderRequestBean
import com.coding.task.order.service.OrderManagementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.coding.task.order.service", "com.coding.task.order.stubs"])
class OrderManagementApplication: CommandLineRunner {

	@Autowired
	private lateinit var orderService: OrderManagementService

	override fun run(vararg args: String?) {

		val items: ArrayList<String> = ArrayList<String>()
		for (arg in args) {
			for (item in arg!!.split(",")){
				items.add(item.toString())
			}
		}

		orderService.placeOrder(items)
	}
}


fun main(args: Array<String>) {
    runApplication<OrderManagementApplication>(*args)
}
