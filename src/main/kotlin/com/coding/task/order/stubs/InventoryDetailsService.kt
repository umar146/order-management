package com.coding.task.order.stubs

import com.coding.task.order.beans.InventoryInfoBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class InventoryDetailsService {

    /**
     * Logger Object
     */
    var logger: Logger = LoggerFactory.getLogger(InventoryDetailsService::class.java)

    /**
     * Customer Information Object
     */
    var inventoryInfo: HashMap<String?, InventoryInfoBean> = HashMap<String?, InventoryInfoBean>()

    /**
     * Method to initialize the Customer Information
     */
    @PostConstruct
    fun initializeInventoryInfo() {

        // Read the Customer Information
        val inventoryInfoContent = this::class.java.classLoader.getResource("price-info.json").readText()

        // Convert the Customer Information into list
        val inventoryInfoList: List<InventoryInfoBean> = Gson().fromJson<List<InventoryInfoBean>>(inventoryInfoContent, object : TypeToken<List<InventoryInfoBean>>() {}.type)

        // Iterate the list and populate the map for in-memory caching the details
        for (inventory in inventoryInfoList) {
            inventoryInfo.put(inventory.name!!.toLowerCase(), inventory);
        }
    }

    /**
     * Method for fetching all the customers
     */
    fun fetchItems(): HashMap<String?, InventoryInfoBean> {
        logger.info("Fetching all the inventories")
        return inventoryInfo
    }

    /**
     * Method for fetching the customer for the given customer id.
     */
    fun fetchItemByName(name: String?): InventoryInfoBean? {
        logger.info("Fetching the Product details for - {}", name)
        return inventoryInfo.get(name)
    }
}