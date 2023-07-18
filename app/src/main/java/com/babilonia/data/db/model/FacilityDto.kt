package com.babilonia.data.db.model

import com.babilonia.EmptyConstants
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Created by Anton Yatsenko on 07.06.2019.
open class FacilityDto constructor() : RealmObject() {

    @PrimaryKey
    var id: Int = EmptyConstants.EMPTY_INT
    var key: String? = null
    var title: String? = null
    var icon: ThumbsDto? = null
}