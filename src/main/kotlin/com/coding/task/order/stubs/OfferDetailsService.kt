package com.coding.task.order.stubs

import com.coding.task.order.beans.OfferDetailsBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class OfferDetailsService {

    /**
     * Logger Object
     */
    var logger: Logger = LoggerFactory.getLogger(InventoryDetailsService::class.java)

    /**
     * Customer Information Object
     */
    var offerDetails: HashMap<Int?, OfferDetailsBean> = HashMap<Int?, OfferDetailsBean>()

    /**
     * Method to initialize the Customer Information
     */
    @PostConstruct
    fun initializeOfferInfo() {

        // Read the Customer Information
        val offerDetailsContent = this::class.java.classLoader.getResource("offer-details.json").readText()

        // Convert the Customer Information into list
        val offerDetailsList: List<OfferDetailsBean> = Gson().fromJson<List<OfferDetailsBean>>(offerDetailsContent, object : TypeToken<List<OfferDetailsBean>>() {}.type)

        // Iterate the list and populate the map for in-memory caching the details
        for (offer in offerDetailsList) {
            offerDetails.put(offer.id, offer);
        }
    }

    /**
     * Method for fetching all the customers
     */
    fun fetchOffers(): HashMap<Int?, OfferDetailsBean> {
        return offerDetails
    }

    /**
     * Method for fetching the customer for the given customer id.
     */
    fun fetchOfferById(offerId: Int?): OfferDetailsBean? {
        return offerDetails[offerId]
    }
}