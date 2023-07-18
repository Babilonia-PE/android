package com.babilonia.data.model.ar

import com.babilonia.data.model.ar.tag.MovableArObject

data class ArState(
    val azimuth: Float,
    val arObjects: List<MovableArObject>
)



