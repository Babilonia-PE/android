package com.babilonia.data.network.error

// Created by Anton Yatsenko on 06.06.2019.
open class NetworkException(error: Throwable) : RuntimeException(error)

class NoNetworkException(error: Throwable) : NetworkException(error)

class ServerUnreachableException(error: Throwable) : NetworkException(error)

class HttpCallFailureException(error: Throwable) : NetworkException(error)

