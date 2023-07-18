package com.babilonia.data.db.model

import com.babilonia.EmptyConstants
import io.realm.RealmObject

// Created by Anton Yatsenko on 07.06.2019.
open class ImageDto : RealmObject() {
    var id: Int = EmptyConstants.EMPTY_INT
    var photo: ThumbsDto? = null
    var created_at: String? = null
}