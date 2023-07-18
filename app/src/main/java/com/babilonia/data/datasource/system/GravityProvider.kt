package com.babilonia.data.datasource.system

import io.reactivex.Observable

interface GravityProvider {
    fun getGravity(): Observable<Float>
}