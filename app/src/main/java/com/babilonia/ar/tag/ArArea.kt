package com.babilonia.ar.tag

import android.graphics.RectF
import android.widget.FrameLayout
import com.babilonia.ar.base.IArea
import com.babilonia.ar.base.ITag
import com.babilonia.data.model.ar.Sizebale
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ArArea(private val view: FrameLayout) : IArea {

    private val tags = HashSet<ITag>()

    val dataUpdateDisposable = CompositeDisposable()

    override var tag: Any? = null
        set(value) {
            dataUpdateDisposable.clear()

            if (value != null) {
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

    override var viewFrame: RectF = RectF(0.0f, 0.0f, 0.0f, 0.0f)
        set(value) {
            view.apply {
                x = value.left
                y = value.top
            }
            field = value
        }

    override fun add(tag: ITag) {
        // Think about how to get view
        tags.add(tag)
    }

    override fun remove(tag: ITag) {
        // Think about how to get view
        tags.remove(tag)
    }

    override fun restore() {
        view.removeAllViews()
        viewFrame = RectF(0.0f, 0.0f, 0.0f, 0.0f)
    }
}