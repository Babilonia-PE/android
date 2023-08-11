package com.babilonia.data.db.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Created by Anton Yatsenko on 06.06.2019.
open class UserDto : RealmObject() {
    @PrimaryKey
    var id: Long? = null
    var phoneNumber: String? = null
    var fullName: String? = null
    var avatar: ThumbsDto? = null
    var email: String? = null
}