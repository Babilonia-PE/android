package com.babilonia.data.db.model

import io.realm.RealmObject

open class ContactDto : RealmObject() {
    var contactName: String? = null
    var contactEmail: String? = null
    var contactPhone: String? = null
}