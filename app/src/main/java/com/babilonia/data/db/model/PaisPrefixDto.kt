package com.babilonia.data.db.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PaisPrefixDto: RealmObject() {
    @PrimaryKey
    var name: String? = null
    var prefix: String? = null
    var mask: String? = null
    var isoCode: String? = null
}