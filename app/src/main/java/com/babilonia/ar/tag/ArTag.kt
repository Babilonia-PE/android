package com.babilonia.ar.tag

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.babilonia.R
import com.babilonia.ar.base.ITag
import com.babilonia.ar.base.TagOptions
import com.babilonia.data.model.ar.Movable
import com.babilonia.data.model.ar.Sizebale
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_ar_tag_small.view.*

class ArTag(private val arView: View) : ITag {

    init {
        arView.tag = this
    }

    val dataUpdateDisposable = CompositeDisposable()

    override var tag: Any? = null
        set(value) {
            dataUpdateDisposable.clear()

            if (value != null) {
                when (value) {
                    is Movable -> {
                        dataUpdateDisposable.add(
                            value.azimuthObservable
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { azimuth -> this.azimuth = azimuth }
                        )
                        dataUpdateDisposable.add(
                            value.distanceObservable
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { distance -> this.distance = distance }
                        )
                    }
                }
                when (value) {
                    is Sizebale -> {
                        dataUpdateDisposable.add(value.sizeObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { frame -> this.viewFrame = frame })
                    }
                }
            }
            field = value
        }

    override var viewFrame: RectF = RectF(
        HIDDEN_POSITION_COORDS,
        HIDDEN_POSITION_COORDS,
        HIDDEN_POSITION_COORDS,
        HIDDEN_POSITION_COORDS
    )
        set(value) {
            arView.apply {
                x = value.left
                y = value.top
                if (value.width() > 0 && value.height() > 0) {
                    isVisible = true
                }
            }
            field = value
        }

    override var azimuth: Double = 0.0

    override var distance: Double = Double.MAX_VALUE
        set(value) {
            if (value != field) {
                arView.apply {
                    tvTagDistanceCount.text = value.toInt().toString()
                }
                field = value
            }
        }

    override var tagOptions = TagOptions()

    override var visible: Boolean
        get() = arView.isVisible
        set(value) {
            arView.isVisible = value
        }

    override fun remove() {
        dataUpdateDisposable.dispose()
        arView.parent?.run { (this as ViewGroup).removeView(arView) }
    }

    override fun setDescription(description: String) {
        arView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        arView.findViewById<AppCompatTextView>(R.id.tvTagDistanceCount).text = description
    }

    override fun select() {
        arView.isSelected = true
    }

    override fun unSelect() {
        arView.isSelected = false
    }

    companion object {
        const val HIDDEN_POSITION_COORDS = -3000f
    }
}