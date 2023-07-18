package com.babilonia.domain.usecase.base

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

// Created by Anton Yatsenko on 24.05.2019.
abstract class BaseReactiveUseCase {

    protected val threadExecutorScheduler: Scheduler = Schedulers.io()

    protected val postExecutionThreadScheduler: Scheduler = AndroidSchedulers.mainThread()

    private val disposables = CompositeDisposable()

    open fun dispose() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }
}