package com.babilonia.domain.model.payment

import com.babilonia.domain.model.enums.PaymentPlanKey

data class PaymentPlan(
    val key: PaymentPlanKey,
    val title: String,
    val descriptions: List<String>,
    val products: List<AdProduct>
)