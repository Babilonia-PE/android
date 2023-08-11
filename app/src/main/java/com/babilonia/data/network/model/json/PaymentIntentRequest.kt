package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

/** Created by Renso Contreras on 22/10/2021.
 * rensocontreras91@gmail.com
 * Lima, Peru.
 **/

class PaymentIntentRequest(
    @SerializedName("request") val request: String,
    @SerializedName("listing_id") val listingId: Long,
    @SerializedName("product_key") val productKey: String?,
    //@SerializedName("publisher_role") val publisherRole: String,
    //@SerializedName("client_id") val clientId: Long
)