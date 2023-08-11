package com.babilonia.data.mapper

import com.babilonia.data.db.model.ThumbsDto
import com.babilonia.data.db.model.UserDto
import com.babilonia.data.network.model.json.ThumbsJson
import com.babilonia.data.network.model.json.UserJson
import com.babilonia.domain.model.User
import javax.inject.Inject

// Created by Anton Yatsenko on 20.06.2019.
class UserMapper @Inject constructor() : Mapper<UserDto, UserJson, User> {
    override fun mapRemoteToLocal(from: UserJson): UserDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: UserDto): UserJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: User): UserDto {
        return UserDto().apply {
            id = from.id
            phoneNumber = from.phoneNumber
            fullName = from.fullName
            avatar = ThumbsDto().apply {
                thumbMiddle = from.avatar
            }
            email = from.email
        }
    }

    override fun mapDomainToRemote(from: User): UserJson {
        return UserJson().apply {
            id = from.id
            phoneNumber = from.phoneNumber
            fullName = from.fullName
            avatar = ThumbsJson().apply {
                thumbMiddle = from.avatar
            }
            email = from.email
        }
    }

    override fun mapLocalToDomain(from: UserDto): User = User(
        from.id,
        from.phoneNumber,
        from.fullName,
        from.avatar?.thumbMin,
        from.email
    )

    override fun mapRemoteToDomain(from: UserJson): User = User(
        from.id,
        from.phoneNumber,
        from.fullName,
        from.avatar?.thumbMin,
        from.email
    )

}