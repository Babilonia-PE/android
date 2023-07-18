package com.babilonia.presentation.flow.main.search.filters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.databinding.ListItemFacilityBinding
import com.babilonia.domain.model.Facility
import com.babilonia.presentation.extension.loadSvg
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilityChangeListener

class FilterFacilitiesAdapter(
    private val onItemClickListener: FacilityChangeListener,
    private val onHeaderClick: (Boolean) -> Unit,
    headerText: String
) : RecyclerView.Adapter<FilterFacilitiesAdapter.ViewHolder>() {

    private val data = mutableListOf<Facility>()
    private val headerData: Facility

    init {
        setHasStableIds(true)
        headerData = Facility(
            id = -1,
            key = null,
            title = headerText,
            icon = null,
            isChecked = false
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_facility, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> bindHeaderViewHolder(holder)
            TYPE_ITEM -> bindItemViewHolder(holder, position)
        }
    }

    override fun getItemId(position: Int): Long =
       if (position == 0) {
           headerData.id.toLong()
        } else {
            data[position - 1].id.toLong()
        }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_HEADER else TYPE_ITEM

    fun addAll(newData: List<Facility>) {
        headerData.isChecked = false
        data.clear()
        if (newData.isNotEmpty()) {
            data.addAll(newData)
        }
        notifyDataSetChanged()
    }

    fun resetAllFacilities() {
        headerData.isChecked = false
        data.forEach {
            it.isChecked = false
        }
        notifyDataSetChanged()
    }

    fun onItemChecked() {
        if (headerData.isChecked.not()) {
            var allIsChecked = true
            for (item in data) {
                if (item.isChecked.not()) {
                    allIsChecked = false
                    break
                }
            }
            if (allIsChecked) {
                headerData.isChecked = true
                notifyItemChanged(0)
            }
        }
    }

    private fun getItemData(position: Int): Facility = data[position - 1]

    private fun bindHeaderViewHolder(holder: ViewHolder) {
        holder.binding?.let { binding ->
            binding.model = headerData
            holder.itemView.setOnClickListener {
                val newValue = binding.cbFacility.isChecked.not()
                binding.cbFacility.isChecked = newValue
                headerData.isChecked = newValue
                data.forEach {
                    it.isChecked = newValue
                }
                notifyDataSetChanged()
                onHeaderClick(newValue)
            }
            binding.ivFacility.setImageResource(R.drawable.ic_facilities_all_24)
            binding.executePendingBindings()
        }
    }

    private fun bindItemViewHolder(holder: ViewHolder, position: Int) {
        val model = getItemData(position)
        holder.binding?.let { binding ->
            binding.model = model
            holder.itemView.setOnClickListener {
                val newValue = binding.cbFacility.isChecked.not()
                binding.cbFacility.isChecked = newValue
                model.isChecked = newValue
                onItemClickListener.onChange(model)
                if (newValue) {
                    onItemChecked()
                } else {
                    onItemUnchecked()
                }
            }
            binding.ivFacility.loadSvg(model.icon)
            binding.executePendingBindings()
        }
    }

    private fun onItemUnchecked() {
        if (headerData.isChecked) {
            headerData.isChecked = false
            notifyItemChanged(0)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListItemFacilityBinding? = DataBindingUtil.bind(itemView)

    }

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER = 1
    }
}