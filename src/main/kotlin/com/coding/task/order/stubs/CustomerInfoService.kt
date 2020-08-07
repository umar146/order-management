package com.coding.task.order.stubs

import com.coding.task.order.beans.CustomerInfoBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class CustomerInfoService {

    /**
     * Logger Object
     */
    var logger: Logger = LoggerFactory.getLogger(CustomerInfoService::class.java)

    /**
     * Customer Information Object
     */
    var customerInfo: HashMap<Int?, CustomerInfoBean> = HashMap<Int?, CustomerInfoBean>()

    /**
     * Method to initialize the Customer Information
     */
    @PostConstruct
    fun initializeCustomerInfo() {

        // Read the Customer Information
        val customerInfoContent = this::class.java.classLoader.getResource("customer-info.json").readText()

        // Convert the Customer Information into list
        val customerInfoList: List<CustomerInfoBean> = Gson().fromJson<List<CustomerInfoBean>>(customerInfoContent, object : TypeToken<List<CustomerInfoBean>>() {}.type)

        // Iterate the list and populate the map for in-memory caching the details
        for (customer in customerInfoList) {
            customerInfo.put(customer.id, customer);
        }
    }

    /**
     * Method for fetching all the customers
     */
    fun fetchCustomers(): HashMap<Int?, CustomerInfoBean> {
        logger.info("Fetching all the customers")
        return customerInfo
    }

    /**
     * Method for fetching the customer for the given customer id.
     */
    fun fetchCustomerById(customerId: Int?): CustomerInfoBean? {
        logger.info("Fetching the customer by id - {}", customerId)
        return customerInfo.get(customerId)
    }
}