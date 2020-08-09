package com.coding.task.order.service.impl

import com.coding.task.order.beans.OrderRequestBean
import com.coding.task.order.beans.OrderResponseBean
import com.coding.task.order.constants.OrderServiceConstants.Companion.QUEUE_NAME
import com.coding.task.order.constants.OrderServiceConstants.Companion.ROUTING_KEY_USER_IMPORTANT_INFO
import com.coding.task.order.constants.OrderServiceConstants.Companion.TOPIC_EXCHANGE_NAME
import com.coding.task.order.service.OrderManagementService
import com.coding.task.order.stubs.CustomerInfoService
import com.coding.task.order.stubs.InventoryDetailsService
import com.coding.task.order.stubs.OfferDetailsService
import com.google.gson.Gson
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
     * RabbitMQ Host Name
     */
    @Value("\${spring.rabbitmq.host}")
    var hostname: String? = null

    /**
     * RabbitMQ Port
     */
    @Value("\${spring.rabbitmq.port}")
    var port: String? = null

    /**
     * RabbitMQ User Name
     */
    @Value("\${spring.rabbitmq.username}")
    var username: String? = null

    /**
     * RabbitMQ Password
     */
    @Value("\${spring.rabbitmq.password}")
    private val password: String? = null


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

            // Populate the Response Information
            response.name = request?.name

            var isValid = true
            var validationMsg: String = ""
            val gson = Gson()

            // Fetch & Populate the Customer details in the response
            var customerInfo = customerService.fetchCustomerById(request!!.customerId)
            response.customer = customerInfo

            // Validate the order request
            when (validateInput(request)) {
                1 -> {
                    isValid = false
                    validationMsg = ("Error while placing the order - Request not valid.")
                }
                2 -> {
                    isValid = false
                    validationMsg = ("Error while placing the order - Customer Information not valid.")
                }
                3 -> {
                    isValid = false
                    validationMsg = ("Error while placing the order - Name not valid.")
                }
                4 -> {
                    isValid = false
                    validationMsg = ("Error while placing the order - Input is not valid.")
                }
                else -> {
                    isValid = true
                }
            }

            if (!isValid) {

                response.status = "Failed"
                response.message = validationMsg

                logger.info(response.message)

                // Publish to RabbitMQ
                /**
                 * Step 3: Publishing the order details to the Customer via MQ. Here I've used the RabbitMQ
                 */
                rabbitSender(gson.toJson(response).toString())
                return response
            }

            // For calculating the total amount
            var totalAmount = 0.0

            // Defining immutable item details map
            val itemDetails: HashMap<String?, Int?> = HashMap()


            // Iterate the order requested items & calculate the requested count
            for (item in request!!.items!!) {
                if (itemDetails[item] != null) {
                    itemDetails[item] = itemDetails[item]?.plus(1)
                } else {
                    itemDetails[item] = 1
                }
            }

            // Iterate the item details & process the order
            for (item in itemDetails) {

                // Invoke the item from the inventory
                val bean = inventoryService.fetchItemByName(item.key?.toLowerCase())

                // Check whether the requested item is available in inventory
                if (bean != null) {

                    var itemCount = item.value
                    var discountedItemCount = 0F

                    // Check whether the item has any offers
                    if (bean.offerId != null) {

                        // Fetch the Offer details
                        var offerDetails = offerDetailsService.fetchOfferById(bean.offerId)

                        // Process the offers & ajust the calculations
                        when (offerDetails?.id) {
                            1 -> {
                                discountedItemCount = ((itemCount?.div(2) ?: 0) + itemCount?.rem(2)!!).toFloat()
                            }
                            2 -> {
                                discountedItemCount = ((itemCount?.div(3) ?: 0) * 2 + itemCount?.rem(3)!!).toFloat()
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

                    /**
                     * Step 4: Handling the scenario where stocks which can be out
                     */
                    if (itemCount!! > bean.available!!) {

                        response.status = "Failed"
                        response.message = "Order placement failed. Following items are not " +
                                "available for the requested quantity: " + item.key + ". Requested: " + itemCount + " Available: " + bean.available

                        logger.info(response.message)

                        // Publish to RabbitMQ
                        rabbitSender(gson.toJson(response).toString())
                        return response

                    }

                    totalAmount += bean!!.price!! * discountedItemCount
                } else {

                    response.status = "Failed"
                    response.message = "Order placement failed. Following items are not " +
                            "available as requested: " + item.key

                    logger.info(response.message)

                    // Publish to RabbitMQ
                    /**
                     * Step 3: Publishing the order details to the Customer via MQ. Here I've used the RabbitMQ
                     */
                    rabbitSender(gson.toJson(response).toString())
                    return response
                }
            }

            response.status = "Completed"
            response.message = "Order placed successfully. Total amount is $$totalAmount"
            response.totalAmount = totalAmount
            response.estimatedDelivery = "Within 2-3 days"

            // Publish to RabbitMQ
            /**
             * Step 3: Publishing the order details to the Customer via MQ. Here I've used the RabbitMQ
             */
            rabbitSender(gson.toJson(response).toString())
            logger.info("Order placed successfully. Total amount is {}", totalAmount)


        } catch (e: Exception) {

            e.printStackTrace()

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
     * @param args
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun placeOrder(args: ArrayList<String>?): OrderResponseBean? {

        // Initialize the request
        val request = OrderRequestBean()
        val items: ArrayList<String> = ArrayList()
        for (item in args!!) {
            if (item.trim().isNotEmpty()) {
                items.add(item)
            }
        }
        request.name = "Sample Order"
        request.customerId = 1
        request.items = items

        // Invoke the place order operation
        return this.placeOrder(request)
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

    /**
     * Function which will send the trasnfer the message to the RabbitMQ topic
     */
    @Throws(Exception::class)
    fun rabbitSender(payload: String) {

        try {
            val factory = ConnectionFactory()
            factory.setUri("amqp://$username:$password@$hostname:$port")
            factory.virtualHost = "/"
            factory.isAutomaticRecoveryEnabled = true

            val connection: Connection = factory.newConnection()
            val channel: Channel = connection.createChannel()

            channel.queueDeclare(QUEUE_NAME, false, false, false, null)
            channel.basicPublish(TOPIC_EXCHANGE_NAME, ROUTING_KEY_USER_IMPORTANT_INFO, null, payload.toByteArray())

            channel.close()
            connection.close()
        } catch (e: Exception){
            logger.error("Unabled to connect to MQ Server.")
        }
    }
}
