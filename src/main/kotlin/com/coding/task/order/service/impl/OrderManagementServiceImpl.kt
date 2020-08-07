package com.coding.task.order.service.impl

import com.coding.task.order.beans.OrderRequestBean
import com.coding.task.order.beans.OrderResponseBean
import com.coding.task.order.service.OrderManagementService
import com.coding.task.order.stubs.CustomerInfoService
import com.coding.task.order.stubs.InventoryDetailsService
import com.coding.task.order.stubs.OfferDetailsService
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class OrderManagementServiceImpl : OrderManagementService {

    /**
     * Logger Object
     */
    var logger: Logger = LoggerFactory.getLogger(OrderManagementService::class.java)

    /**
     * Customer Information Service
     */
    @Autowired
    private lateinit var customerService: CustomerInfoService

    /**
     * Inventory Details Service
     */
    @Autowired
    private lateinit var inventoryService: InventoryDetailsService

    /**
     * Inventory Details Service
     */
    @Autowired
    private lateinit var offerDetailsService: OfferDetailsService

    /**
     *
     */
    val topic: String = "test_topic"

    /**
     * For placing the order for the given request
     * @param request
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun placeOrder(request: OrderRequestBean?): OrderResponseBean? {

        // Initialize the response
        val response = OrderResponseBean()

        try {

            // Validate the order request
            when (validateInput(request)) {
                1 -> throw Exception("Error while placing the order - Request not valid.")
                2 -> throw Exception("Error while placing the order - Customer Information not valid.")
                3 -> throw Exception("Error while placing the order - Name not valid.")
                4 -> throw Exception("Error while placing the order - Input is not valid.")
                else -> logger.info("Order Request is valid. Proceeding further to place the order.")
            }

            var totalAmount = 0.0;

            var itemDetails: HashMap<String?, Int?> = HashMap<String?, Int?>()

            // Populate the Response Information
            response.name = request?.name




            for (item in request!!.items!!) {
                if (itemDetails[item] != null) {
                    itemDetails.put(item, itemDetails[item]?.plus(1))
                } else {
                    itemDetails.put(item, 1)
                }
            }

            for (item in itemDetails) {

                // Invoke the item from the inventory
                val bean = inventoryService.fetchItemByName(item.key?.toLowerCase());

                if (bean != null) {

                    var itemCount = item.value;
                    var discountedItemCount: Float = 0F

                    if (bean.offerId != null) {

                        var offerDetails = offerDetailsService.fetchOfferById(bean.offerId)

                        when (offerDetails?.id) {
                            1 -> {
                                discountedItemCount = ((itemCount?.div(2) ?:0) + itemCount?.rem(2)!!).toFloat()
                            }
                            2 -> {
                                discountedItemCount = ((itemCount?.div(3) ?:0) * 2 + itemCount?.rem(3)!!).toFloat()
                            }
                            3 -> {
                                discountedItemCount = itemCount?.div(10)?.times(100f) ?: 0F
                            }
                            4 -> {
                                discountedItemCount = itemCount?.div(50)?.times(100f) ?: 0F
                            }
                            else -> {
                                discountedItemCount = itemCount?.times(1F)!!
                            }
                        }
                    }

                    totalAmount += bean!!.price!! * discountedItemCount!!;
                } else {

                    response.status = "Failed"
                    response.message = "Order placement failed. Following items are not " +
                            "available as requested: " + item.key

                    logger.info(response.message);

                    return response
                }
            }

            response.status = "Completed"
            response.message = "Order placed successfully. Total amount is $$totalAmount"
            response.totalAmount = totalAmount

            logger.info("Order placed successfully. Total amount is {}", totalAmount);


        } catch (e: Exception) {

            // Log & throw the exception
            if (request != null) {

                logger.error("Exception occurred while placing the order for Customer: {} and Name: {}",
                        request.customerId, request.name)
            }
        }

        // Return the response
        return response
    }

    /**
     * For placing the order for the given request
     * @param items
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun placeOrder(args: ArrayList<String>?): OrderResponseBean? {

        // Initialize the request
        val request = OrderRequestBean()
        val items: ArrayList<String> = ArrayList<String>()
        for (item in args!!) {
            items.add(item)
        }
        request.name = "Sample Order"
        request.customerId = 1
        request.items = items

        // Invoke the place order operation
        return this.placeOrder(request);
    }

    /**
     * Method for validating the Input Request. Following conditions are validated.,
     */
    fun validateInput(request: OrderRequestBean?): Int? {

        // Check whether the request bean is available or not
        if (request == null) {
            return 1
        }

        // Check whether the Customer ID is available
        if (request.customerId == null || request.customerId!! <= 0) {
            return 2
        }

        // Check whether the Order Name is available
        if (request.name == null || request.name!!.trim().isEmpty()) {
            return 3
        }

        // Check whether the Items are available
        if (request.items == null || request.items!!.isEmpty()) {
            return 4
        }

        // Return '-1' when there is no problem in the input request
        return -1
    }
}
