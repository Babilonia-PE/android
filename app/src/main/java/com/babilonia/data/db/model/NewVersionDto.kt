package com.babilonia.data.db.model

import io.realm.RealmObject

open class NewVersionDto : RealmObject() {
    var android: Boolean? = null
}