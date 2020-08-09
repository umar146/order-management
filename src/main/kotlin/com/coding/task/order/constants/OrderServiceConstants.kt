package com.coding.task.order.constants

/**
 * Constant definitions
 */
class OrderServiceConstants {
    companion object {
        final const val ROUTING_KEY_USER_IMPORTANT_WARN = "user.important.warn"
        final const val ROUTING_KEY_USER_IMPORTANT_INFO = "user.important.info"
        final const val TOPIC_EXCHANGE_NAME = "orders.topic.exchange"
        final const val QUEUE_NAME = "orders.topic.queue"
        final const val BINDING_PATTERN_IMPORTANT = "*.important.*"
    }
}