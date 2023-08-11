package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

/** Created by Renso Contreras on 22/10/2021.
 * rensocontreras91@gmail.com
 * Lima, Peru.
 **/

class DoPaymentRequest(
    @SerializedName("deviceSession_id") val deviceSessionId: String?,
    @SerializedName("payment_type") val paymentType: String,
    @SerializedName("card_number") val cardNumber: String,
    @SerializedName("order_id") val orderId: Long?,
    @SerializedName("document_type") val documentType: String,
    @SerializedName("card_cvv") val cardCvv: String,
    @SerializedName("card_expiration") val cardExpiration: String,
    @SerializedName("card_name") val cardName: String)