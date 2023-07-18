package com.babilonia.domain.utils

import com.babilonia.data.model.ar.navigator.Navigatable

interface ArTagTypeProvider {
    fun getArTagType(navigatable: Navigatable): ArTagType
}