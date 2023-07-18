package com.babilonia.android.rotation

class LowPassFilter(private val alpha: Float = 0.15F) {

    fun applyFilter(source: FloatArray, target: FloatArray) {

        // Do not apply a filter if source array length less than the target array length
        if (source.size < target.size) return

        for (i in 0 until source.size) {
            target[i] += alpha * (source[i] - target[i])
        }
    }
}