package com.babilonia.presentation.flow.main.payment.plan

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.presentation.extension.inflate
import kotlinx.android.synthetic.main.vh_payment_plan.view.*

class SelectPaymentPlanAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = arrayListOf<PaymentPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            PlanViewHolder(parent.inflate(R.layout.vh_payment_plan))
        } else {
            FooterViewHolder(parent.inflate(R.layout.vh_payment_plan_footer))
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ITEM) {
            (holder as PlanViewHolder).bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) TYPE_FOOTER else TYPE_ITEM
    }

    fun setItems(newItems: List<PaymentPlan>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getRealItemCount() = items.size

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(model: PaymentPlan) {
            itemView.paymentPlan.setData(model)
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }
}