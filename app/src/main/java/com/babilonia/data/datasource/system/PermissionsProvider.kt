package com.babilonia.data.datasource.system

import io.reactivex.Completable
import io.reactivex.Observable

interface PermissionsProvider {

    var preRequestClarification: Completable

    fun isGranted(permission: String): Boolean
    fun requestPermissions(vararg permissions: String): Observable<Boolean>
}