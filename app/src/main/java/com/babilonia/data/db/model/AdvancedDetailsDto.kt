package com.babilonia.data.db.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AdvancedDetailsDto constructor() : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var data: RealmList<FacilityDto> = RealmList()
}