package com.babilonia.data.model

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error<out T>(val throwable: Throwable) : DataResult<T>()
}