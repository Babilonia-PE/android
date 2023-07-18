package com.babilonia.data.db.model

import com.babilonia.EmptyConstants
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Created by Anton Yatsenko on 15.0@7.2019.
open class AppConfigDto : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var locationDto: LocationDto? = null
    var privacyPolicy: String = EmptyConstants.EMPTY_STRING
    var terms: String = EmptyConstants.EMPTY_STRING
}