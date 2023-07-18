package com.babilonia.data.db.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Created by Anton Yatsenko on 20.06.2019.
open class FacilitiesDto constructor() : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var data: RealmList<FacilityDto> = RealmList()
}