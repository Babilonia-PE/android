package com.babilonia.domain.usecase.base

import com.babilonia.presentation.base.BaseViewModel
import io.reactivex.observers.DisposableObserver
import java.lang.ref.WeakReference


// Created by Anton Yatsenko on 27.05.2019.
abstract class ObservableErrorWrapper<T>(view: BaseViewModel) : DisposableObserver<T>() {
    //BaseView is just a reference of a View in MVP
    private val weakReference: WeakReference<BaseViewModel> = WeakReference(view)

    protected abstract fun onSuccess(t: T)

    override fun onNext(t: T) {
        //You can return StatusCodes of different cases from your API and handle it here. I usually include these cases on BaseResponse and iherit it from every Response
        onSuccess(t)
    }

    //TODO: Customize error handling
    override fun onError(e: Throwable) {
        val view = weakReference.get()
        view?.dataError?.postValue(e)
    }

    override fun onComplete() {

    }
}