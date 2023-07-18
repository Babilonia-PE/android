package com.babilonia.data.db.model

import io.realm.RealmObject

// Created by Anton Yatsenko on 18.06.2019.
open class ThumbsDto : RealmObject() {

    var url: String? = null
    var thumbMin: String? = null
    var thumbMiddle: String? = null
    var thumbLarge: String? = null


}