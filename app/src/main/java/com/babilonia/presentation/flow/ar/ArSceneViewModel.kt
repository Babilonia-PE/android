package com.babilonia.presentation.flow.ar

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.ar.base.ITag
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.ar.Sizebale
import com.babilonia.data.model.ar.tag.ArObject
import com.babilonia.data.model.ar.tag.MovableArObject
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.GetRouteUseCase
import com.babilonia.domain.usecase.GetUserIdUseCase
import com.babilonia.domain.usecase.ListingActionUseCase
import com.babilonia.domain.usecase.ar.ArNavigationSubscriberUseCase
import com.babilonia.domain.usecase.ar.CurrentLocationSubscriberUseCase
import com.babilonia.domain.usecase.ar.GetArStateSubscriberUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.createlisting.ID
import com.babilonia.presentation.flow.main.publish.createlisting.MODE
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ArSceneViewModel @Inject constructor(
    private val getArStateUseCase: GetArStateSubscriberUseCase,
    private val arNavigationUseCase: ArNavigationSubscriberUseCase,
    private val locationUseCase: CurrentLocationSubscriberUseCase,
    private val getRouteUseCase: GetRouteUseCase,
    private val listingActionUseCase: ListingActionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : BaseViewModel() {

    private val locationRequest = LocationRequest(
        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
        LocationRequest.LOCATION_UPDATE_INTERVAL_SMALL
    )
    private val arTagsMap = hashMapOf<Long, ITag>()

    private val isNavigationMode = AtomicBoolean(false)

    val selectedTag = MutableLiveData<ITag?>()
    val destinationTag = MutableLiveData<MovableArObject?>()
    val modeLiveData = MutableLiveData<ArScreenMode>()
    var navigationTagId = EmptyConstants.EMPTY_LONG

    val tagsLiveData = MutableLiveData<MovableArObject>()
    val currentLocationLiveData = MutableLiveData<ILocation>()
    val mapRotation = MutableLiveData<ArState>()

    val contactOwnerLiveData = SingleLiveEvent<String>()
    val routeLiveData = MutableLiveData<List<RouteStep>>()
    val userIdLiveData = MutableLiveData<Long>()

    lateinit var params: Params

    init {
        modeLiveData.postValue(ArScreenMode.SEARCH)
        getUserId()
    }

    fun stopObserving() {
        getArStateUseCase.clear()
        arNavigationUseCase.clear()
        locationUseCase.clear()
        clearArTags()
    }

    private fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
                userIdLiveData.value = userId
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun onFavouriteClicked(isChecked: Boolean, id: Long) {
        val mode = if (isChecked) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }

        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(id, ListingAction.FAVOURITE, mode))
    }

    fun subscribeToArState(
        cameraProjectionMatrix: FloatArray,
        sceneWidth: Int,
        sceneHeight: Int,
        horizontalSpace: Float,
        verticalSpace: Float
    ) {
        params = Params(
            locationRequest = locationRequest,
            cameraProjectionMatrix = cameraProjectionMatrix,
            sceneWidth = sceneWidth,
            sceneHeight = sceneHeight,
            horizontalSpace = horizontalSpace,
            verticalSpace = verticalSpace
        )

        subscribeToCurrentLocation(getCurrentLocationUseCaseParams(), getCurrentLocationObserver())

        if (navigationTagId == EmptyConstants.EMPTY_LONG) {
            arNavigationUseCase.clear()
            subscribeToArState(getArStateUseCaseParams(), getArStateObserver())
        } else {
            getArStateUseCase.clear()
            subscribeToArNavigation(getArNavigationUseCaseParams(navigationTagId), getArNavigationObserver())
        }
    }

    fun contactOwner(id: Long, phoneNumber: String) {
        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                contactOwnerLiveData.value = phoneNumber
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(
            id,
            ListingAction.CONTACT_VIEW,
            ListingActionMode.SET
        ))
    }

    private fun subscribeToArState(
        params: GetArStateSubscriberUseCase.Params,
        observer: DisposableObserver<DataResult<ArState>>
    ) {
        isNavigationMode.set(false)
        getArStateUseCase.subscribe(observer, params)
    }

    private fun subscribeToArNavigation(
        params: ArNavigationSubscriberUseCase.Params,
        observer: DisposableObserver<DataResult<ArState>>
    ) {
        isNavigationMode.set(true)
        arNavigationUseCase.subscribe(observer, params)
    }

    private fun subscribeToCurrentLocation(
        params: CurrentLocationSubscriberUseCase.Params,
        observer: DisposableObserver<DataResult<ILocation>>
    ) {
        locationUseCase.subscribe(observer, params)
    }

    fun getRoute(currentLocation: ILocation) {
        destinationTag.value?.listing?.locationAttributes?.let { listingLocation ->
            getRouteUseCase.execute(object : DisposableSingleObserver<List<RouteStep>>() {
                override fun onSuccess(route: List<RouteStep>) {
                    routeLiveData.value = route
                }

                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                }
            }, GetRouteUseCase.Params(currentLocation, listingLocation))
        }
    }

    private fun actualizeState(arState: ArState) {
        val movableArObjects = arState.arObjects

        val arTagsForRemoving = arTagsMap.filterKeys { key ->
            val local = arTagsMap[key]
            val remote = movableArObjects.firstOrNull { key == it.id }

            if (remote == null || remote.arTagSizeType != (local?.tag as? Sizebale)?.arTagSizeType) {
                local?.apply {
                    visible = false
                    remove()
                }
                return@filterKeys true
            }

            return@filterKeys false
        }

        arTagsMap.minusAssign(arTagsForRemoving.keys)

        movableArObjects.forEach { arTagsMap[it.id]?.apply { tag = it } ?: tagsLiveData.postValue(it) }
    }

    private fun updateDestination(arState: ArState) {
        if (!arState.arObjects.isNullOrEmpty()) {

            val movableArObject = arState.arObjects[0]

            if (destinationArrivalValidation(movableArObject)) {

                messageEvent.postValue(SuccessMessageType.DESTINATION_REACHED)
                switchToSearchMode()
            } else {

                mapRotation.postValue(arState)

                var needToAddTag = false

                if (arTagsMap.containsKey(movableArObject.id)) {
                    arTagsMap[movableArObject.id]?.let {
                        val tagSize = (it.tag as Sizebale).arTagSizeType
                        if (tagSize != movableArObject.arTagSizeType) {
                            needToAddTag = true
                            it.remove()
                        } else {
                            it.tag = movableArObject
                        }
                    }
                }  else {
                    needToAddTag = true
                }

                if (needToAddTag) {
                    tagsLiveData.postValue(movableArObject)
                    destinationTag.postValue(movableArObject)
                }
            }

        } else {
            clearArTags()
        }
    }


    fun addMapTag(id: Long, tag: ITag) {
        arTagsMap[id] = tag
    }

    fun selectTag(tag: ITag) {
        if (!isNavigationMode.get()) {
            selectedTag.value?.unSelect()

            val selectedArId = (selectedTag.value?.tag as? MovableArObject)?.id
            val tagArId = (tag.tag as? MovableArObject)?.id ?: EmptyConstants.EMPTY_LONG

            if (tagArId != selectedArId) {
                selectedTag.value = tag
                tag.select()
            } else {
                selectedTag.value = null
            }
        }
    }

    fun unselectTag() {
        if (!isNavigationMode.get()) {
            selectedTag.value?.unSelect()
            selectedTag.value = null
        }
    }

    fun navigateToDetails(id: Long) {
        navigate(
            R.id.action_ar_to_listingFragment,
            bundleOf(
                ID to id,
                MODE to ListingDisplayMode.IMPROPER_LISTING
            )
        )
    }

    fun navigateToMap() {
        navigate(
            R.id.action_ar_to_mapFragment,
            bundleOf(
                ID to navigationTagId
            )
        )
    }

    fun switchToNavigationMode() {
        (selectedTag.value?.tag as? MovableArObject)?.also { selection ->

            if (selection.distance <= Constants.DESTINATION_AREA_RADIUS_METERS) {

                messageEvent.postValue(SuccessMessageType.DESTINATION_REACHED)
            } else {

                selection.id.also { objectId ->
                    clearArTags(objectId)
                    modeLiveData.value = ArScreenMode.NAVIGATION
                    navigationTagId = objectId
                    destinationTag.postValue(selection)
                    getArStateUseCase.clear()
                    subscribeToCurrentLocation(getCurrentLocationUseCaseParams(), getCurrentLocationObserver())
                    subscribeToArNavigation(getArNavigationUseCaseParams(objectId), getArNavigationObserver())
                }
            }
        }
    }

    fun handleBackPress() {
        if (destinationTag.value != null) {
            switchToSearchMode()
        }
    }

    fun switchToSearchMode() {
        modeLiveData.value = ArScreenMode.SEARCH
        destinationTag.value = null
        navigationTagId = EmptyConstants.EMPTY_LONG
        arNavigationUseCase.clear()
        subscribeToArState(getArStateUseCaseParams(), getArStateObserver())
    }

    private fun getArStateUseCaseParams(): GetArStateSubscriberUseCase.Params {
        return GetArStateSubscriberUseCase.Params(
            locationRequest = params.locationRequest,
            cameraProjectionMatrix = params.cameraProjectionMatrix,
            sceneWidth = params.sceneWidth,
            sceneHeight = params.sceneHeight,
            horizontalSpace = params.horizontalSpace,
            verticalSpace = params.verticalSpace
        )
    }

    private fun getArNavigationUseCaseParams(navigationTagId: Long): ArNavigationSubscriberUseCase.Params {
        return ArNavigationSubscriberUseCase.Params(
            locationRequest = params.locationRequest,
            cameraProjectionMatrix = params.cameraProjectionMatrix,
            sceneWidth = params.sceneWidth,
            sceneHeight = params.sceneHeight,
            selectedId = navigationTagId
        )
    }

    private fun getCurrentLocationUseCaseParams(): CurrentLocationSubscriberUseCase.Params {
        return CurrentLocationSubscriberUseCase.Params(locationRequest = params.locationRequest)
    }

    private fun clearArTags(vararg excludeIds: Long) {
        synchronized(arTagsMap) {
            val excludes = arrayListOf<ITag>()
            arTagsMap.forEach { (arObjectId, tag) ->
                if (arObjectId in excludeIds) {
                    excludes.add(tag)
                } else {
                    tag.visible = false
                    tag.remove()
                }
            }
            arTagsMap.clear()
            excludes.forEach { arTagsMap[(it.tag as? ArObject)?.id ?: 0] = it }
        }
    }


    private fun getArStateObserver(): DisposableObserver<DataResult<ArState>> {
        return getObserver { actualizeState(it) }
    }

    private fun getArNavigationObserver(): DisposableObserver<DataResult<ArState>> {
        return getObserver { updateDestination(it) }
    }

    private fun getCurrentLocationObserver(): DisposableObserver<DataResult<ILocation>> =
        object : DisposableObserver<DataResult<ILocation>>() {

            override fun onComplete() {
                // empty
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onNext(result: DataResult<ILocation>) {
                when (result) {
                    is DataResult.Success -> currentLocationLiveData.postValue(result.data)
                    is DataResult.Error -> result.throwable.printStackTrace()
                }
            }
        }

    private fun getObserver(onNext: (arState: ArState) -> Unit): DisposableObserver<DataResult<ArState>> =
        object : DisposableObserver<DataResult<ArState>>() {

            override fun onComplete() {
                // empty
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onNext(result: DataResult<ArState>) {
                when (result) {
                    is DataResult.Success -> onNext(result.data)
                    is DataResult.Error -> result.throwable.printStackTrace()
                }
            }
        }

    private fun destinationArrivalValidation(destination: MovableArObject): Boolean {
        return destination.distance <= Constants.DESTINATION_AREA_RADIUS_METERS
    }

    class Params(
        val locationRequest: LocationRequest,
        val cameraProjectionMatrix: FloatArray,
        val sceneWidth: Int,
        val sceneHeight: Int,
        val horizontalSpace: Float,
        val verticalSpace: Float
    )
}


