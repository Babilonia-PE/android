package com.babilonia.data.network.error

import com.babilonia.EmptyConstants
import com.babilonia.ErrorKeys
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Created by Anton Yatsenko on 06.06.2019.
//Flowable
fun <T> Flowable<T>.mapNetworkErrors(): Flowable<T> =
    this.onErrorResumeNext { error: Throwable ->
        if (error is HttpException && error.code() == 401) {
            val msg = mapHttpToBaseError(error)?.getMessage() ?: error.message()
            Flowable.error(AuthFailedException(msg))
        } else {
            when (error) {
                is SocketTimeoutException -> Flowable.error(NoNetworkException(error))
                is UnknownHostException -> Flowable.error(ServerUnreachableException(error))
                is HttpException -> Flowable.error(HttpCallFailureException(error))
                else -> Flowable.error(error)
            }
        }
    }

inline fun <T> Flowable<T>.mapErrors(): Flowable<T> =
    this.map { it }
        .onErrorResumeNext { error: Throwable ->
            if (error is HttpException && error.code() >= 400) {
                mapErrorBody(error).let {
                    Flowable.error<T>(Throwable(it))
                }
            } else {
                Flowable.error<T>(error)
            }
        }

//Single
fun <T> Single<T>.mapNetworkErrors(): Single<T> =
    this.onErrorResumeNext { error ->
        if (error is HttpException && error.code() == 401) {
            val msg = mapHttpToBaseError(error)?.getMessage() ?: error.message()
            Single.error(AuthFailedException(msg))
        } else {
            when (error) {
                is SocketTimeoutException -> Single.error(NoNetworkException(error))
                is UnknownHostException -> Single.error(ServerUnreachableException(error))
                is HttpException -> Single.error(error)
                else -> Single.error(error)
            }
        }
    }

fun <T> Single<T>.mapErrors(): Single<T> =
    this.map { it }
        .onErrorResumeNext { error ->
            if (error is HttpException && error.code() >= 400) {
                val baseError = mapHttpToBaseError(error)
                when (baseError?.getKey()) {
                    ErrorKeys.KEY_AUTH_FAILED -> Single.error<T>(AuthFailedException(baseError.getMessage()))
                    ErrorKeys.KEY_NOT_FOUND -> Single.error<T>(NotFoundException(baseError.getMessage()))
                    ErrorKeys.KEY_EMAIL_ALREADY_TAKEN -> Single.error<T>(EmailAlreadyTakenException())
                    ErrorKeys.KEY_ALREADY_PUBLISHED -> Single.error<T>(AlreadyPublishedException(baseError.getMessage()))
                    else -> Single.error<T>(Throwable(baseError?.getMessage()))
                }
            } else {
                Single.error<T>(error)
            }
        }

//Completable

fun Completable.mapNetworkErrors(): Completable =
    this.onErrorResumeNext { error ->
        if (error is HttpException && error.code() == 401) {
            val msg = mapHttpToBaseError(error)?.getMessage() ?: error.message()
            Completable.error(AuthFailedException(msg))
        } else {
            when (error) {
                is SocketTimeoutException -> Completable.error(NoNetworkException(error))
                is UnknownHostException -> Completable.error(ServerUnreachableException(error))
                is HttpException -> Completable.error(HttpCallFailureException(error))
                else -> Completable.error(error)
            }
        }
    }

fun Completable.mapErrors(): Completable =
    this.onErrorResumeNext { error ->
        if (error is HttpException && error.code() >= 400) {
            Completable.error(Exception(mapErrorBody(error)))
        } else {
            Completable.error(error)
        }
    }

fun mapHttpToBaseError(error: HttpException): BaseError? {
    return Gson().fromJson(
        error.response()?.errorBody()?.string(),
        BaseError::class.java
    )
}

fun mapErrorBody(error: HttpException): String {
    return mapHttpToBaseError(error)?.getMessage() ?: EmptyConstants.EMPTY_STRING
}
