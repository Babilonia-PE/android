package com.babilonia.presentation.flow.main.publish.createlisting

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import com.babilonia.R
import com.babilonia.data.model.DataResult
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.*
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.utils.ImageUtil
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
    private val getDefaultLocationUseCase: GetDefaultLocationUseCase
) :
    BaseViewModel() {

    val gotFacilitiesEvent = SingleLiveEvent<List<Facility>>()
    val gotAdvancedDetailsEvent = SingleLiveEvent<List<Facility>>()
    val imageUploadedEvent = SingleLiveEvent<ListingImage>()
    val gotListingEvent = SingleLiveEvent<Listing>()
    val locationLiveData = SingleLiveEvent<ILocation>()
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
        getListingUseCase.execute(
            object : DisposableSingleObserver<Listing>() {
                override fun onSuccess(listing: Listing) {
                    gotListingEvent.value = listing
                }

                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                }

            }, GetListingUseCase.Params(id, mode)
        )
    }

    fun updateListing(params: Listing) {
        updateListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(updatedListing: Listing) {
                navigateBack()
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
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
                        Timber.e(throwable)
                        loadingEvent.postValue(false)
                    }
                )
        )
    }

    fun uploadImage(path: String) {
        uploadListingImageUseCase.execute(object : DisposableSingleObserver<ListingImage>() {
            override fun onSuccess(listinImage: ListingImage) {
                imageUploadedEvent.postValue(listinImage)
                loadingEvent.postValue(false)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
                loadingEvent.postValue(false)
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
        super.onCleared()
    }
}
