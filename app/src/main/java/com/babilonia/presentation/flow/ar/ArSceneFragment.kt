package com.babilonia.presentation.flow.ar

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.ar.LocationHelper
import com.babilonia.ar.base.ITag
import com.babilonia.ar.tag.ArTag
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.ar.tag.MovableArObject
import com.babilonia.databinding.ArSceneFragmentBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.safeLet
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.withGlide
import com.babilonia.presentation.flow.ar.ArScreenMode.NAVIGATION
import com.babilonia.presentation.flow.ar.ArScreenMode.SEARCH
import com.babilonia.presentation.flow.main.listing.common.ListingImagesPagerAdapter
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.view.ArTagViewFactory
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tbruyelle.rxpermissions2.RxPermissions
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.selector.*
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ar_scene_bottom_details.*
import kotlinx.android.synthetic.main.ar_scene_fragment.*
import kotlinx.android.synthetic.main.layout_ar_navigation_preview.*
import timber.log.Timber
import java.lang.Math.toDegrees
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.atan2

class ArSceneFragment : BaseFragment<ArSceneFragmentBinding, ArSceneViewModel>(), OnMapReadyCallback {

    private lateinit var fotoapparat: Fotoapparat

    @Inject
    lateinit var arTagViewFactory: ArTagViewFactory

    private var permissionsDisposable: Disposable? = null

    private var animationSet = ConstraintSet()

    private var googleMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null

    private var cameraDirection = 0f

    private var polyline: Polyline? = null

    private val backPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.handleBackPress()
        }
    }

    private val mapDataObserver = Observer<ArState> { handleMapData(it) }
    private val selectedArTagObserver = Observer<ITag?> { arTag ->
        if (arTag == null) {
            hideBottomListing()
        } else {
            (arTag.tag as? MovableArObject)?.apply { showBottomListing(listing) }
            updateNavigateButton()
        }
    }
    private val destinationTagObserver = Observer<MovableArObject?> { movableArObject ->
        movableArObject?.geoData?.objectLocation?.apply {
            showMapMarker(this)
        }
    }
    private val arTagsObserver = Observer<MovableArObject> { movableArObject ->
        renderTag(movableArObject).apply {
            viewModel.addMapTag(movableArObject.id, this)
        }
    }
    private val showDetailsClickListener = View.OnClickListener {
        showSelectedListingDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.tagsLiveData.observe(this, arTagsObserver)
        viewModel.modeLiveData.observe(this, Observer { mode ->
            when (mode) {
                SEARCH -> switchToSearchMode()
                NAVIGATION -> switchToNavigationMode()
                else -> {
                    /*nothing to do*/
                }
            }
        })
        viewModel.currentLocationLiveData.observe(this, Observer { location ->
            val mode = viewModel.modeLiveData.value
            if (mode != null && mode == NAVIGATION) {
                showCurrentLocation(location)
                viewModel.getRoute(location)
            } else {
                updateNavigateButton()
            }
        })
        viewModel.contactOwnerLiveData.observe(this, Observer { navigateToCallScreen(it) })
        viewModel.routeLiveData.observe(this, Observer { setPath(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ar_scene_fragment, container, false)
    }

    override fun viewCreated() {
        fotoapparat = Fotoapparat(
            context = requireContext(),
            view = cameraView,
            lensPosition = back(),
            cameraConfiguration = CameraConfiguration(
                previewFpsRange = highestFps(),
                focusMode = firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),
                    fixed()
                ),
                flashMode = off()
            ),
            logger = logcat(),
            cameraErrorCallback = { it.printStackTrace() }
        )

        ivArrowHide.setOnClickListener { viewModel.unselectTag() }
        ivBack.setOnClickListener { requireActivity().onBackPressed() }
        btnGoToMap.setOnClickListener { viewModel.navigateToMap() }
    }

    override fun onStart() {
        super.onStart()
        permissionsDisposable = RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .subscribe { isGranted ->
                if (isGranted) {
                    fotoapparat.start()

                    arScene.post {
                        val matrix = generateProjectionMatrix(arScene.width, arScene.height)
                        viewModel.subscribeToArState(
                            matrix,
                            arScene.width,
                            arScene.height,
                            resources.getDimension(R.dimen.ar_tag_horizontal_indent),
                            resources.getDimension(R.dimen.ar_tag_vertical_indent)
                        )
                    }

                    mapFragment = childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
                    mapFragment?.getMapAsync(this)
                } else {
                    handleRejectPermissions()
                }
            }

        handleFragmentResume()
    }

    override fun onStop() {
        super.onStop()
        permissionsDisposable?.dispose()
        viewModel.stopObserving()
        runCatching { fotoapparat.stop() }
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map?.also {
            it.uiSettings?.also { settings ->
                settings.isCompassEnabled = false
                settings.isMapToolbarEnabled = false
                settings.isZoomControlsEnabled = false
                settings.setAllGesturesEnabled(false)
            }

            it.setOnMapClickListener {}
        }
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressCallback)
    }

    override fun handleSuccessEvent(type: SuccessMessageType) {
        super.handleSuccessEvent(type)
        if (type == SuccessMessageType.DESTINATION_REACHED) {
            showSnackbar(R.string.destination_reached)
        }
    }

    private fun handleFragmentResume() {
        val mode = viewModel.modeLiveData.value
        if (mode != null && mode == NAVIGATION) {
            circleLayoutMapContainer.isVisible = true
            viewModel.destinationTag.value?.let {
                showListingPreview(it.listing)
                renderTag(it).apply {
                    viewModel.addMapTag(it.id, this)
                }
            }
            viewModel.currentLocationLiveData.value?.let {
                viewModel.getRoute(it)
            }
        } else {
            viewModel.selectedTag.value?.let {
                val arTag = it.tag as? MovableArObject
                if (arTag?.listing != null) {
                    showBottomListing(arTag.listing)
                }
            }
        }
    }

    private fun handleMapData(arState: ArState) {
        if (circleLayoutMapContainer.isVisible) {
            rotateMap(arState.azimuth)
            if (arState.arObjects.isNotEmpty()) {
                rotateArrow(arState.arObjects[0].geoData.objectLocation)
                displayMapDistance(arState.arObjects[0].geoData.distance)
            }
        }
    }

    private fun rotateMap(azimuth: Float) {
        if (circleLayoutMapContainer.isVisible) {
            googleMap?.apply {
                cameraDirection = azimuth
                val camPos = CameraPosition
                    .builder(cameraPosition)
                    .bearing(azimuth)
                    .build()
                moveCamera(CameraUpdateFactory.newCameraPosition(camPos))
            }
        }
    }

    private fun rotateArrow(location: ILocation) {
        if (circleLayoutMapContainer.isVisible) {
            googleMap?.apply {
                projection.toScreenLocation(LatLng(location.latitude, location.longitude))
                    .apply {
                        val deltaX = x - ivArrowDirection.x - ivArrowDirection.width / 2
                        val deltaY = y - ivArrowDirection.y - ivArrowDirection.height / 2
                        val degrees = toDegrees(atan2(deltaY, deltaX).toDouble()).toFloat()
                        ivArrowDirection.rotation = degrees + 90f
                    }
            }
        }
    }

    private fun displayMapDistance(distance: Double) {
        tvMapDistance.text = distance.toInt().toString()
    }

    private fun showCurrentLocation(location: ILocation) {
        googleMap?.apply {
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(location.latitude, location.longitude))
                .zoom(17f)
                .bearing(cameraDirection)
                .build()
            moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun renderTag(movableArObject: MovableArObject): ITag = arTagViewFactory
        .inflateView(arScene, movableArObject)
        .let {
            arScene.addView(it)
            ArTag(it)
                .apply {
                    tag = movableArObject
                    it.setOnClickListener { viewModel.selectTag(this) }
                }
        }

    private fun switchToSearchMode() {
        googleMap?.clear()
        polyline = null

        circleLayoutMapContainer.isVisible = false
        tvTitle.text = getString(R.string.ar_screen_title_view)

        backPressCallback.isEnabled = false

        viewModel.selectedTag.observe(this, selectedArTagObserver)

        viewModel.mapRotation.removeObserver(mapDataObserver)
        viewModel.destinationTag.removeObserver(destinationTagObserver)
        hideListingPreview()
    }

    private fun switchToNavigationMode() {
        hideBottomListing()
        circleLayoutMapContainer.isVisible = true
        tvTitle.text = getString(R.string.ar_screen_title_navigation)

        backPressCallback.isEnabled = true

        viewModel.selectedTag.removeObserver(selectedArTagObserver)

        viewModel.mapRotation.observe(this, mapDataObserver)
        viewModel.destinationTag.observe(this, destinationTagObserver)
        viewModel.currentLocationLiveData.value?.let { viewModel.getRoute(it) }
    }

    private fun showMapMarker(location: ILocation) {
        googleMap?.apply {
            val bitmap =
                (ContextCompat.getDrawable(requireContext(), R.drawable.ic_ar_pin_house) as? BitmapDrawable)
                    ?.bitmap
                    ?.run {
                        Bitmap.createScaledBitmap(
                            this,
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_width),
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_height),
                            false
                        )
                    }

            val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude))

            addMarker(
                if (bitmap == null) {
                    markerOptions
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }
            )
        }
    }

    private fun setPath(route: List<RouteStep>) {
        val path = arrayListOf<LatLng>()
        route.forEach { step ->
            path.add(step.startLocation)
            path.add(step.endLocation)
        }
        if (polyline == null) {
            val newPolylineOptions = PolylineOptions()
            newPolylineOptions.width(Constants.ROUTE_POLYLINE_WIDTH)
            context?.let { newPolylineOptions.color(ContextCompat.getColor(it, R.color.topaz)) }
            newPolylineOptions.geodesic(false)
            newPolylineOptions.pattern(listOf(Dot(), Gap(Constants.ROUTE_POLYLINE_GAP_SIZE)))
            googleMap?.let {
                polyline = it.addPolyline(newPolylineOptions)
            }
        }
        polyline?.points = path
    }

    private fun generateProjectionMatrix(width: Int, height: Int): FloatArray {
        // TODO move to const
        val Z_NEAR = 0.5F
        val Z_FAR = 10_000F
        val OFFSET = 0
        var bottom = -1F
        var top = 1F
        var left = -1F
        var right = 1F

        val projectionMatrix = FloatArray(16)

        if (width > height) {
            val ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            val ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }


        Matrix.frustumM(projectionMatrix, OFFSET, left, right, bottom, top, Z_NEAR, Z_FAR)
        return projectionMatrix
    }

    private fun handleRejectPermissions() {
        // TODO Need implementation based on business logic
        Timber.tag("TEST_PERMISSION").d("Permission denied")
    }

    private val numberFormat = NumberFormat.getInstance(Locale.US)

    private fun showListingPreview(listing: Listing) {
        clNavigationPreview.visible()
        with (listing) {
            getPreviewImageUrl()?.let {
                ivPreviewPhoto.withGlide(it)
            }
            tvPreviewPrice.text = getString(R.string.price_template, numberFormat.format(price))
            tvPreviewPropertyType.text = PropertyType.getLocalizedPropertyName(resources, propertyType)
            tvPreviewAddress.text = locationAttributes.address
            setNavigationPreviewCounters(this)

            btnExitPreview.setOnClickListener {
                viewModel.switchToSearchMode()
                showBottomListing(this)
                updateNavigateButton()
            }
        }
    }

    private fun hideListingPreview() {
        clNavigationPreview.invisible()
    }

    private fun showBottomListing(listing: Listing) {
        listing.apply {
            val typeUpperCase = context?.let {
                ListingUtilsDelegateImpl.getLocalizedType(it, listing.listingType)
            }
            tvPropertyType.text = PropertyType.getLocalizedPropertyName(resources, propertyType)
            tvListingType.text = getString(R.string.prefix_for, typeUpperCase)
            tvImagesCount.text = (images?.size ?: 0).toString()
            tvPrice.text = getString(R.string.price_template, numberFormat.format(price))
            setListingPreviewCounters(this)
            tvAddress.text = listing.locationAttributes.address

            ivPropertyType.setImageResource(ListingUtilsDelegateImpl.getListingIconByType(propertyType))

            btnShowDetails.setOnClickListener(showDetailsClickListener)
            vListingDetails.setOnClickListener(showDetailsClickListener)

            if (viewModel.userIdLiveData.value == user?.id) {
                cbFavorite.invisible()
            } else {
                cbFavorite.visible()
                cbFavorite.isChecked = isFavourite
                cbFavorite.setOnClickListener {
                    id?.let { it -> viewModel.onFavouriteClicked(cbFavorite.isChecked, it) }
                }
            }

            btnNavigate.setOnClickListener {
                viewModel.switchToNavigationMode()
                showListingPreview(this)
            }

            btnContact.setOnClickListener {
                safeLet(listing.user?.id, listing.user?.phoneNumber) { userId, phoneNumber ->
                    showContactDialog(userId, phoneNumber)
                }
            }

            vpImages.adapter = ListingImagesPagerAdapter(images, 16) {
                showSelectedListingDetails()
            }
            pagerIndicator.attachToPager(vpImages)

            price?.let { price ->
                area?.let { area ->
                    tvSubPrice.text = getString(R.string.meter_price_template, (price / area).toString())
                }
            }

            when (adPlan) {
                PaymentPlanKey.PLUS -> {
                    ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_yellow_24)
                    ivPlanIcon.visible()
                }
                PaymentPlanKey.PREMIUM -> {
                    ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_blue_24)
                    ivPlanIcon.visible()
                }
                else -> ivPlanIcon.invisible()
            }
        }

        animationSet.apply {
            clone(clRoot)
            clear(clBottomListing.id, ConstraintSet.TOP)
            connect(clBottomListing.id, ConstraintSet.BOTTOM, clRoot.id, ConstraintSet.BOTTOM)
            applyTo(clRoot)
        }

        TransitionManager.beginDelayedTransition(clRoot)
    }

    private fun hideBottomListing() {
        animationSet.apply {
            clone(clRoot)
            clear(clBottomListing.id, ConstraintSet.BOTTOM)
            connect(clBottomListing.id, ConstraintSet.TOP, clRoot.id, ConstraintSet.BOTTOM)
            applyTo(clRoot)
        }

        TransitionManager.beginDelayedTransition(clRoot)
    }

    private fun updateNavigateButton() {
        val placeLocation = (viewModel.selectedTag.value?.tag as? MovableArObject)?.location
        safeLet(placeLocation, viewModel.currentLocationLiveData.value) { placeLoc, userLoc ->
            val distance = LocationHelper.distanceBetween(userLoc, placeLoc)
            if (distance <= Constants.DESTINATION_AREA_RADIUS_METERS) {
                if (btnNavigate.isVisible) {
                    enableContactButton()
                }
            } else {
                if (btnContact.isVisible) {
                    enableNavigateButton()
                }
            }
        }
    }

    private fun enableNavigateButton() {
        btnNavigate.visible()
        btnContact.invisible()
    }

    private fun enableContactButton() {
        btnNavigate.invisible()
        btnContact.visible()
    }

    private fun showContactDialog(userId: Long, phoneNumber: String) {
        context?.let {
            StyledAlertDialog.Builder(it)
                .setTitleText(phoneNumber)
                .setRightButton(getString(R.string.call)) {
                    viewModel.contactOwner(userId, phoneNumber)
                }
                .setLeftButton(getString(R.string.cancel))
                .build()
                .show()
        }
    }

    private fun navigateToCallScreen(phoneNumber: String) {
        startActivity(
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
        )
    }

    private fun showSelectedListingDetails() {
        viewModel.selectedTag.value?.let {  arTag ->
            val listingId = (arTag.tag as? MovableArObject)?.listing?.id
            if (listingId != null) {
                viewModel.navigateToDetails(listingId)
            }
        }
    }

    private fun setListingPreviewCounters(listing: Listing) {
        with (listing) {
            if (bedroomsCount != null && bedroomsCount != 0) {
                tvCountBedroom.visible()
                ivFirstDivider.visible()
                tvCountBedroom.text = getString(R.string.beds_numb_small, bedroomsCount)
            } else {
                tvCountBedroom.invisible()
                ivFirstDivider.invisible()
            }
            if (bathroomsCount != null && bathroomsCount != 0) {
                tvCountBathroom.visible()
                ivSecondDivider.visible()
                tvCountBathroom.text = getString(R.string.bath_numb_small, bathroomsCount)
            } else {
                tvCountBathroom.invisible()
                ivSecondDivider.invisible()
            }
            if (area != null && area != 0) {
                tvTotalArea.visible()
                tvTotalArea.text = getString(R.string.area_numb, area.toString())
            } else {
                tvTotalArea.invisible()
                if (ivSecondDivider.isVisible) {
                    ivSecondDivider.invisible()
                } else {
                    ivFirstDivider.invisible()
                }
            }
        }
    }

    private fun setNavigationPreviewCounters(listing: Listing) {
        with (listing) {
            if (bedroomsCount != null && bedroomsCount != 0) {
                tvPreviewCountBedroom.visible()
                ivPreviewFirstDivider.visible()
                tvPreviewCountBedroom.text = getString(R.string.beds_numb_small, bedroomsCount)
            } else {
                tvPreviewCountBedroom.invisible()
                ivPreviewFirstDivider.invisible()
            }
            if (bathroomsCount != null && bathroomsCount != 0) {
                tvPreviewCountBathroom.visible()
                ivPreviewSecondDivider.visible()
                tvPreviewCountBathroom.text = getString(R.string.bath_numb_small, bathroomsCount)
            } else {
                ivPreviewSecondDivider.invisible()
                tvPreviewCountBathroom.invisible()
            }
            if (area != null && area != 0) {
                tvPreviewTotalArea.visible()
                tvPreviewTotalArea.text = getString(R.string.area_numb, area.toString())
            } else {
                tvPreviewTotalArea.invisible()
                if (ivPreviewSecondDivider.isVisible) {
                    ivPreviewSecondDivider.invisible()
                } else {
                    ivPreviewFirstDivider.invisible()
                }
            }
        }
    }
}
