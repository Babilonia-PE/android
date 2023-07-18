package com.babilonia.data.network

import com.babilonia.data.network.service.AuthService

// Created by Anton Yatsenko on 07.06.2019.
//Fix of dependency cycle
class TokenServiceHolder {
    lateinit var authService: AuthService
}