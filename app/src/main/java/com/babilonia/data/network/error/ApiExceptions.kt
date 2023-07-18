package com.babilonia.data.network.error

open class ApiException(message: String? = null): Throwable(message)

class AuthFailedException(message: String? = null): ApiException(message)

class NotFoundException(message: String? = null): ApiException(message)

class EmailAlreadyTakenException(message: String? = null) : ApiException(message)

class AlreadyPublishedException(message: String? = null): ApiException(message)