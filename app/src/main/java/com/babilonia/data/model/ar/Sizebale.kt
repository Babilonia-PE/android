package com.babilonia.data.model.ar

import android.graphics.RectF
import com.babilonia.domain.utils.ArTagType
import io.reactivex.Observable

interface Sizebale {
    var sizeObservable: Observable<RectF>
    var tagRect: RectF
    var arTagSizeType: ArTagType
}