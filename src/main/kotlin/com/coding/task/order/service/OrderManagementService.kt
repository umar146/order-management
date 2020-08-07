package com.coding.task.order.service

import com.coding.task.order.beans.OrderRequestBean
import com.coding.task.order.beans.OrderResponseBean


/**
 * Interface definition for the Order Management Operations.
 */
interface OrderManagementService {

    /**
     * For placing the order for the given request
     * @param request
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun placeOrder(request: OrderRequestBean?): OrderResponseBean?

    /**
     * For placing the order for the given list of items
     */
    @Throws(Exception::class)
    fun placeOrder(args: ArrayList<String>?): OrderResponseBean?
}