package com.babilonia.domain.usecase.base

import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber

// Created by Anton Yatsenko on 10.06.2019.
abstract class FlowableUseCase<Results, in Params> : BaseReactiveUseCase() {

    /**
     * Builds an [Flowable] which will be used when executing the current [FlowableUseCase].
     */
    abstract fun buildUseCaseFlowable(params: Params): Flowable<Results>

    /**
     * Executes the current use case.
     *
     * @param observer [FlowableUseCase] which will be listening to the observer build
     * by [buildUseCaseFlowable] method.
     * @param params Parameters (Optional) used to build/execute this use case.
     */
    fun execute(observer: DisposableSubscriber<Results>, params: Params) {
        val observable = buildUseCaseObservableWithSchedulers(params)
        addDisposable(observable.subscribeWith(observer))
    }

    /**
     * Builds an [Flowable] which will be used when executing the current [FlowableUseCase].
     * With provided Schedulers
     */
    private fun buildUseCaseObservableWithSchedulers(params: Params): Flowable<Results> {
        return buildUseCaseFlowable(params)
            .subscribeOn(threadExecutorScheduler)
            .observeOn(postExecutionThreadScheduler, true)
    }
}
