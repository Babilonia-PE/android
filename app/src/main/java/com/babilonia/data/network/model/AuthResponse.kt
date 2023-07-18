package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.TokensJson
import com.babilonia.data.network.model.json.UserJson

// Created by Anton Yatsenko on 06.06.2019.
class AuthResponse(var user: UserJson, var tokens: TokensJson)