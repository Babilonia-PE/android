package com.babilonia.android.permission

import androidx.appcompat.app.AppCompatActivity
import com.babilonia.data.datasource.system.PermissionsProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AppPermissionsProvider @Inject constructor(val activity: AppCompatActivity) : PermissionsProvider {

    override var preRequestClarification = Completable.complete()

    override fun isGranted(permission: String) = RxPermissions(activity).isGranted(permission)

    override fun requestPermissions(vararg permissions: String): Observable<Boolean> {
        return Single.fromCallable {
            permissions.map { isGranted(it) }.all { it }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .flatMapObservable { granted ->
                when (granted) {
                    true -> Observable.just(true)
                    else -> preRequestClarification
                        .andThen(RxPermissions(activity).request(*permissions))
                }
            }
    }
}