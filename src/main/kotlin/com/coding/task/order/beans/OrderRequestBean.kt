package com.coding.task.order.beans

/**
 * Bean class for holding the order information for placing the order
 */
data class OrderRequestBean(var name: String? = null, var customerId: Int? = null,
                            var items: List<String>? = null, var additionalInfo: String? = null)