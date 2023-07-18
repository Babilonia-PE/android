package com.babilonia.domain.usecase.base

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

abstract class SubscriberUseCase<R, in P> {

    var compositeSubscription: CompositeDisposable = CompositeDisposable()


    abstract fun buildSubscriptionUseCase(params: P): Observable<R>

    private fun getScheduledSubscription(params: P, transformer: ObservableTransformer<R, R>?): Observable<R> {
        val observable = buildSubscriptionUseCase(params)
        if (transformer != null) {
            return observable.compose(transformer)
        }
        return observable
    }

    fun subscribe(observer: DisposableObserver<R>, params: P) = subscribe(observer, ObservableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }, params)

    fun subscribe(observer: DisposableObserver<R>, transformer: ObservableTransformer<R, R>?, params: P) {
        if (compositeSubscription.size() > 0) return
        compositeSubscription.add(getScheduledSubscription(params, transformer).subscribeWith(observer))
    }

    open fun clear() {
        compositeSubscription.clear()
    }

    open fun dispose() {
        compositeSubscription.dispose()
    }
}