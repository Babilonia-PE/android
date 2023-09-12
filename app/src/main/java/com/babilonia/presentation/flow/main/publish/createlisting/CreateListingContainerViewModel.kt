package com.babilonia.presentation.flow.main.publish.createlisting

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.R
import com.babilonia.data.model.DataResult
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.*
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.*
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.utils.ImageUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val ID = "id"
const val MODE = "mode"

@Singleton
class CreateListingContainerViewModel @Inject constructor(
    private val getFacilitiesUseCase: GetFacilitiesUseCase,
    private val draftListingUseCase: DraftListingUseCase,
    private val uploadListingImageUseCase: UploadListingImageUseCase,
    private val getListingUseCase: GetListingUseCase,
    private val updateListingUseCase: UpdateListingUseCase,
    private val getLastKnownLocationUseCase: GetLastKnownLocationUseCase,
    private val getDefaultLocationUseCase: GetDefaultLocationUseCase,
    private val getUbigeosUseCase: GetUbigeosUseCase,
    private val getUserUseCase: GetUserUseCase,
) : BaseViewModel() {
    val authFailedData = SingleLiveEvent<Unit>()
    val gotFacilitiesEvent = SingleLiveEvent<List<Facility>>()
    val gotAdvancedDetailsEvent = SingleLiveEvent<List<Facility>>()
    val imageUploadedEvent = SingleLiveEvent<ListingImage>()
    val gotListingEvent = SingleLiveEvent<Listing>()
    val locationLiveData = SingleLiveEvent<ILocation>()
    val restartUbigeo = SingleLiveEvent<Boolean>()

    private val _isLoadingListingEvent = SingleLiveEvent<Boolean>().apply { value = false }
    val isLoadingListingEvent: LiveData<Boolean> = _isLoadingListingEvent

    private val _disableComponentsEvent = SingleLiveEvent<Boolean>().apply { value = false }
    val disableComponentsEvent: LiveData<Boolean> = _disableComponentsEvent

    val listDepartments = SingleLiveEvent<List<String>>()
    val listProvinces   = SingleLiveEvent<List<String>>()
    val listDistricts   = SingleLiveEvent<List<String>>()

    private val _isLoadingDepartment = SingleLiveEvent<Boolean>().apply { value = false }
    val isLoadingDepartment: LiveData<Boolean> = _isLoadingDepartment

    private val _isLoadingProvince = SingleLiveEvent<Boolean>().apply { value = false }
    val isLoadingProvince: LiveData<Boolean> = _isLoadingProvince

    private val _isLoadingDistrict = SingleLiveEvent<Boolean>().apply { value = false }
    val isLoadingDistrict: LiveData<Boolean> = _isLoadingDistrict

    val userLiveData = MutableLiveData<User>()

    fun getUser() {
        getUserUseCase.execute(object : DisposableSubscriber<User>() {
            override fun onComplete() {
            }

            override fun onNext(user: User) {
                userLiveData.value = user
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                }
            }
        }, Unit)
    }

    fun navigateToDescription() {
        navigate(CreateListingContainerFragmentDirections.actionCreateListingContainerFragment2ToListingDescriptionFragment())
    }

    fun navigateToPlacePicker() {
        navigate(R.id.action_createListingContainerFragment2_to_placePickerFragment)
    }

    fun navigateToPreview(id: Long) {
        val bundle = bundleOf(ID to id, MODE to ListingDisplayMode.PREVIEW)
        navigate(R.id.action_global_listingFragment, bundle)
    }

    fun getListingsById(id: Long, mode: ListingDisplayMode = ListingDisplayMode.PREVIEW) {
        _isLoadingListingEvent.postValue(true)
        getListingUseCase.execute(
            object : DisposableSingleObserver<Listing>() {
                override fun onSuccess(listing: Listing) {
                    _isLoadingListingEvent.postValue(false)
                    restartUbigeo.postValue(true)
                    gotListingEvent.value = listing
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        _isLoadingListingEvent.postValue(false)
                        restartUbigeo.postValue(false)
                        dataError.postValue(e)
                    }
                }

            }, GetListingUseCase.Params(id, mode)
        )
    }

    fun updateListing(params: Listing) {
        _isLoadingListingEvent.postValue(true)
        _disableComponentsEvent.postValue(true)
        updateListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(updatedListing: Listing) {
                navigateBack()
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else {
                    dataError.postValue(e)
                    _isLoadingListingEvent.postValue(false)
                    _disableComponentsEvent.postValue(false)
                }
            }

        }, params)
    }

    fun getFacilities(type: String) {
        val typeLowercase = type.toLowerCase(Locale.ROOT)
        getFacilitiesByDataType(typeLowercase)
        getAdvancedDetailsByDataType(typeLowercase)
    }

    private fun getFacilitiesByDataType(type: String) {
        getFacilitiesUseCase.execute(object : DisposableSubscriber<List<Facility>>() {
            override fun onComplete() {}

            override fun onNext(facilities: List<Facility>) {
                gotFacilitiesEvent.value = facilities
            }

            override fun onError(e: Throwable) {
                dataError.value = e
            }

        }, GetFacilitiesUseCase.Params(FacilityDataType.FACILITY, type))
    }

    private fun getAdvancedDetailsByDataType(type: String) {
        getFacilitiesUseCase.execute(object : DisposableSubscriber<List<Facility>>() {
            override fun onComplete() {}

            override fun onNext(advancedDetails: List<Facility>) {
                gotAdvancedDetailsEvent.value = advancedDetails
            }

            override fun onError(e: Throwable) {
                dataError.value = e
            }

        }, GetFacilitiesUseCase.Params(FacilityDataType.ADVANCED, type))
    }

    fun createListing(params: Listing, shouldMoveToPreview: Boolean = false, shouldNavigateBack: Boolean = false) {
        draftListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(t: Listing) {
                if (shouldMoveToPreview) {
                    t.id?.let { navigateToPreview(it) }
                } else if (shouldNavigateBack) {
                    navigateBack()
                }
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(e)
            }

        }, params)
    }

    fun compressImageAndUpload(context: Context, uri: Uri) {
        disposables.add(
            ImageUtil.compressBitmap(context, uri)
                .doOnSubscribe {
                    loadingEvent.postValue(true)
                }
                .subscribe(
                    { file ->
                        if (file != null) {
                            uploadImage(file.path)
                        } else {
                            loadingEvent.postValue(false)
                        }
                    }, { throwable ->
                        FirebaseCrashlytics.getInstance().recordException(throwable)
                        Timber.e(throwable)
                        loadingEvent.postValue(false)
                    }
                )
        )
    }

    fun uploadImage(path: String) {
        uploadListingImageUseCase.execute(object : DisposableSingleObserver<List<ListingImage>>() {
            override fun onSuccess(listinImages: List<ListingImage>) {
                imageUploadedEvent.postValue(listinImages[0])
                loadingEvent.postValue(false)
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    dataError.postValue(e)
                    loadingEvent.postValue(false)
                }
            }

        }, path)
    }

    fun clear() {
        onCleared()
    }

    fun getLocation(onLocationFound: ((location: ILocation) -> Unit)? = null) {
        getLastKnownLocationUseCase.execute(object : DisposableSingleObserver<DataResult<ILocation>>() {
            override fun onSuccess(result: DataResult<ILocation>) {
                when (result) {
                    is DataResult.Error -> {
                        getDefaultLocation()
                        dataError.postValue(result.throwable)
                    }
                    is DataResult.Success -> {
                        onLocationFound?.invoke(result.data)
                        locationLiveData.postValue(result.data)
                    }
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, Unit)
    }

    private fun getDefaultLocation() {
        getDefaultLocationUseCase.execute(object : DisposableSingleObserver<Location>() {
            override fun onSuccess(location: Location) {
                locationLiveData.postValue(location)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, Unit)
    }

    override fun onCleared() {
        uploadListingImageUseCase.dispose()
        draftListingUseCase.dispose()
        getFacilitiesUseCase.dispose()
        getUbigeosUseCase.dispose()
        super.onCleared()
    }

    fun getListDepartments(type: String) {
        _isLoadingDepartment.postValue(true)
        _isLoadingProvince.postValue(true)
        _isLoadingDistrict.postValue(true)
        getUbigeosUseCase.execute(
            object : DisposableSingleObserver<List<String>>() {

                override fun onSuccess(ubigeos: List<String>) {
                    _isLoadingDepartment.postValue(false)
                    listDepartments.value = ubigeos
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        _isLoadingDepartment.postValue(false)
                        _isLoadingProvince.postValue(false)
                        _isLoadingDistrict.postValue(false)
                        dataError.postValue(e)
                    }
                }

            }, GetUbigeosUseCase.Params(type, null, null)
        )
    }

    fun getListProvinces(type: String, department: String?) {
        _isLoadingProvince.postValue(true)
        _isLoadingDistrict.postValue(true)
        getUbigeosUseCase.execute(
            object : DisposableSingleObserver<List<String>>() {

                override fun onSuccess(ubigeos: List<String>) {
                    _isLoadingProvince.postValue(false)
                    listProvinces.value = ubigeos
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        _isLoadingProvince.postValue(false)
                        _isLoadingDistrict.postValue(false)
                        dataError.postValue(e)
                    }
                }

            }, GetUbigeosUseCase.Params(type, department, null)
        )
    }

    fun getListDistricts(type: String, department: String?, province: String?) {
        _isLoadingDistrict.postValue(true)
        getUbigeosUseCase.execute(
            object : DisposableSingleObserver<List<String>>() {

                override fun onSuccess(ubigeos: List<String>) {
                    _isLoadingDistrict.postValue(false)
                    listDistricts.value = ubigeos
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        _isLoadingDistrict.postValue(false)
                        dataError.postValue(e)
                    }
                }

            }, GetUbigeosUseCase.Params(type, department, province)
        )
    }

    fun getDepartment(): Array<String> {
        return listDepartments.value?.toTypedArray()?: arrayOf()
    }

    fun getProvince(): Array<String> {
        return listProvinces.value?.toTypedArray()?: arrayOf()
    }

    fun getDistrict(): Array<String> {
        return listDistricts.value?.toTypedArray()?: arrayOf()
    }

    fun resetDepartment() {
        return listDepartments.postValue(listOf())
    }

    fun resetProvince() {
        return listProvinces.postValue(listOf())
    }

    fun resetDistrict() {
        return listDistricts.postValue(listOf())
    }

    fun offLoadingDepartment(){
        _isLoadingDepartment.postValue(false)
        _isLoadingProvince.postValue(false)
        _isLoadingDistrict.postValue(false)
    }

    fun offLoadingProvinces(){
        _isLoadingProvince.postValue(false)
        _isLoadingDistrict.postValue(false)
    }

    fun offLoadingDistricts(){
        _isLoadingDistrict.postValue(false)
    }

    fun restartUbigeoEvent(){
        restartUbigeo.postValue(false)
    }
}
