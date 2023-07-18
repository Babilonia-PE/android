package com.babilonia.presentation.view.payment

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.babilonia.R
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.utils.PriceFormatter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.view_payment_plan.view.*

class PaymentPlanCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_payment_plan, this, true)
    }

    fun setData(paymentPlan: PaymentPlan) {
        tvPlanName.text = paymentPlan.title
        tvPlanDescription.text = paymentPlan.products.firstOrNull()?.let { product ->
            context.getString(
                R.string.from_price,
                String.format(PriceFormatter.PRICE_TWO_DIGITS_AFTER_POINT, product.price)
            )
        }
        when(paymentPlan.key) {
            PaymentPlanKey.STANDARD -> {
                loadImageWithRoundedCorners(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.bg_payment_plan_standart,
                        null
                    )
                )
                ivPlanIcon.invisible()
            }
            PaymentPlanKey.PLUS -> {
                loadImageWithRoundedCorners(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.bg_payment_plan_plus,
                        null
                    )
                )
                ivPlanIcon.visible()
                ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_28)
            }
            PaymentPlanKey.PREMIUM -> {
                loadImageWithRoundedCorners(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.bg_payment_plan_premium,
                        null
                    )
                )
                ivPlanIcon.visible()
                ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_28)
            }
        }
    }

    private fun loadImageWithRoundedCorners(drawable: Drawable?) {
        Glide.with(this)
            .load(drawable)
            .transform(CenterCrop(), RoundedCorners(16))
            .into(ivPlanBackground)
    }
}