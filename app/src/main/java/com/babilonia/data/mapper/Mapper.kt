package com.babilonia.data.mapper

// Created by Anton Yatsenko on 20.06.2019.
interface Mapper<LOCAL, REMOTE, DOMAIN> {
    fun mapDomainToLocal(from: DOMAIN): LOCAL
    fun mapDomainToRemote(from: DOMAIN): REMOTE
    fun mapLocalToDomain(from: LOCAL): DOMAIN
    fun mapRemoteToDomain(from: REMOTE): DOMAIN
    fun mapRemoteToLocal(from: REMOTE): LOCAL
    fun mapLocalToRemote(from: LOCAL): REMOTE
}