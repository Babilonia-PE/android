package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.network.model.json.AdPlanJson
import com.babilonia.data.network.model.json.AdProductJson
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.payment.AdProduct
import com.babilonia.domain.model.payment.PaymentPlan
import javax.inject.Inject

class PaymentPlanMapper @Inject constructor() {
    fun mapRemoteToDomain(planJson: AdPlanJson): PaymentPlan {
        with (planJson) {
            return PaymentPlan(
                key = key?.let { PaymentPlanKey.valueOf(it.toUpperCase()) } ?: PaymentPlanKey.STANDARD,
                title = title ?: EmptyConstants.EMPTY_STRING,
                descriptions = descriptions ?: emptyList(),
                products = products?.map { mapProduct(it) } ?: emptyList()
            )
        }
    }

    private fun mapProduct(productJson: AdProductJson): AdProduct {
        with (productJson) {
            return AdProduct(
                key = key ?: EmptyConstants.EMPTY_STRING,
                duration = duration ?: EmptyConstants.EMPTY_INT,
                price = price ?: EmptyConstants.EMPTY_FLOAT,
                comment = comment ?: EmptyConstants.EMPTY_STRING
            )
        }
    }
}