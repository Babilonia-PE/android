package com.babilonia.presentation.flow.main.payment.plan

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.presentation.extension.inflate
import kotlinx.android.synthetic.main.view_payment_plan.view.*

class SelectPaymentPlanDescriptionAdapter :
    RecyclerView.Adapter<SelectPaymentPlanDescriptionAdapter.PlanDescriptionViewHolder>() {

    private val items = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanDescriptionViewHolder {
        return PlanDescriptionViewHolder(parent.inflate(R.layout.vh_payment_plan_description))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlanDescriptionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun setItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class PlanDescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(description: String) {
            itemView.tvPlanDescription.text = description
        }
    }
}