package com.coding.task.order.beans

import com.google.gson.JsonObject

/**
 * Bean class for holding the response details for order placement
 */
data class OrderResponseBean(var name: String? = null, var items: JsonObject? = null,
                             var status: String? = null, var totalAmount: Double? = null,
                             var message: String? = null, var totalItems: Int? = null)