package com.babilonia.presentation.flow.main.payment.period

import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.domain.model.payment.AdProduct
import com.babilonia.presentation.extension.inflate
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.utils.PriceFormatter
import kotlinx.android.synthetic.main.vh_payment_period.view.*

class SelectPaymentPeriodAdapter :
    RecyclerView.Adapter<SelectPaymentPeriodAdapter.PlanDescriptionViewHolder>() {

    private val items = arrayListOf<AdProduct>()

    var checkedItemIndex = 0
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanDescriptionViewHolder {
        return PlanDescriptionViewHolder(parent.inflate(R.layout.vh_payment_period)).apply {
            itemView.setOnClickListener {
                if (itemView.rbCheck.isChecked.not()) {
                    selectItem(product)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlanDescriptionViewHolder, position: Int) {
        holder.bind(items[position], position == checkedItemIndex)
    }

    fun setItems(newItems: List<AdProduct>) {
        items.clear()
        items.addAll(newItems)
        checkedItemIndex = 0
        notifyDataSetChanged()
    }

    fun selectItem(product: AdProduct) {
        val previousCheckedItem = checkedItemIndex
        for (i in items.indices) {
            if (items[i] == product) {
                checkedItemIndex = i
                break
            }
        }
        notifyItemChanged(checkedItemIndex)
        notifyItemChanged(previousCheckedItem)
    }

    class PlanDescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var product: AdProduct

        fun bind(product: AdProduct, isChecked: Boolean) {

            this.product = product

            with(itemView) {
                tvPeriodDuration.text = context.resources.getQuantityString(
                    R.plurals.plan_duration_days_plural,
                    product.duration,
                    product.duration.toString()
                )
                tvPeriodPrice.text = context.getString(
                    R.string.sol_price_template,
                    String.format(PriceFormatter.PRICE_TWO_DIGITS_AFTER_POINT, product.price))

                if (product.comment.isBlank()) {
                    tvPeriodComment.invisible()
                } else {
                    tvPeriodComment.visible()
                    tvPeriodComment.text = product.comment
                }

                rbCheck.isChecked = isChecked
                tvPeriodPrice.setTextColor(if (isChecked) {
                    ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                } else {
                    ResourcesCompat.getColor(resources, R.color.dark_blue_grey, null)
                })
            }
        }
    }
}