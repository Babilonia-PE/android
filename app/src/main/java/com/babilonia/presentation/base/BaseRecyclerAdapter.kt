package  com.babilonia.presentation.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil.DiffResult.NO_POSITION
import androidx.recyclerview.widget.RecyclerView

// Created by Anton Yatsenko on 26.02.2019.
abstract class BaseRecyclerAdapter<T : ViewDataBinding> : RecyclerView.Adapter<BaseViewHolder<T>>() {
    private var recyclerView: RecyclerView? = null

    protected val preBindCallback: OnRebindCallback<T> = object : OnRebindCallback<T>() {
        override fun onPreBind(binding: T): Boolean {
            if (recyclerView == null || recyclerView?.isComputingLayout == true) return true
            if (binding.root.layoutParams is RecyclerView.LayoutParams) {
                val adapterPosition = recyclerView?.getChildAdapterPosition(binding.root) ?: NO_POSITION
                if (adapterPosition == NO_POSITION) return true

                notifyItemChanged(adapterPosition)
            }

            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val holder = BaseViewHolder.create<T>(parent, viewType)
        holder.binding.addOnRebindCallback(preBindCallback)
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        bindItem(holder, position)
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int = getLayoutId(position)

    abstract fun bindItem(holder: BaseViewHolder<T>, position: Int)

    @LayoutRes
    abstract fun getLayoutId(position: Int): Int

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

}


open class BaseViewHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun <T : ViewDataBinding> create(parent: ViewGroup, @LayoutRes layoutId: Int) =
            BaseViewHolder<T>(DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false))
    }
}