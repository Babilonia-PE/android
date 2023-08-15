package com.babilonia.data.db.model

import io.realm.RealmObject

open class UrlDto : RealmObject() {
    var main: String? = null
    var share: String? = null
}