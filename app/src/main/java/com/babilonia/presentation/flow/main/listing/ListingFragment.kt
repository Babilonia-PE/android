package com.babilonia.presentation.flow.main.listing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.babilonia.BuildConfig
import com.babilonia.Constants
import com.babilonia.Constants.CORNER_RADIUS
import com.babilonia.Constants.MAX_LINES
import com.babilonia.Constants.PUBLISHER_ROLE_OWNER
import com.babilonia.Constants.PUBLISHER_ROLE_REALTOR
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.ListingFragmentBinding
import com.babilonia.domain.model.Contact
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.domain.model.enums.PublishState
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.*
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.listing.common.ListingFacilitiesAdapter
import com.babilonia.presentation.flow.main.listing.common.ListingImagesPagerAdapter
import com.babilonia.presentation.flow.main.payment.PaymentActivity
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.utils.DateFormatter
import com.babilonia.presentation.utils.NetworkUtil
import com.babilonia.presentation.view.CustomTypefaceSpan
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.listing_fragment.*
import java.text.NumberFormat
import java.util.*


class ListingFragment : BaseFragment<ListingFragmentBinding, ListingViewModel>() {
    private val args: ListingFragmentArgs by navArgs()
    private val facilitiesAdapter by lazy { ListingFacilitiesAdapter() }
    private val advancedDetailsAdapter by lazy { ListingFacilitiesAdapter() }

    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.viewLoadedData.value = false
    }

    override fun viewCreated() {
        showProgress()
        if (viewModel.listingId == EmptyConstants.EMPTY_LONG) {
            viewModel.displayMode = args.mode
            viewModel.listingId = args.id
        }

        binding.mode = viewModel.displayMode
        binding.viewModel = viewModel
        binding.ivFavorite.setOnClickListener {
            viewModel.setFavorite(binding.ivFavorite.isChecked)
        }
        setRecycler()
        binding.fabWhatsapp.gone()
        binding.btListingAction.gone()

        viewModel.getUserId()
        if (viewModel.viewLoadedData.value != true) {
            viewModel.getListing(viewModel.displayMode)
        }

        viewModel.ipAddress = NetworkUtil.getIPAddress(requireContext()) ?: ""
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.listingLiveData.observe(this, Observer { listing ->
            if (listing.id == viewModel.listingId) {
                updateDisplayMode(listing)
                setPriceSubtitle(listing)
                setDisplayMode(listing)
                setPetFriendly(listing)
                listing.facilities?.let { setFacilities(it) }
                listing.advancedDetails?.let { setAdvancedDetails(it) }
                setImagesAdapter(listing)
                setPublishDetails(listing)
                setShowMore()
                setUser(listing.user, listing.contacts?.first())
                loadMap(listing)
                setListingType(listing)
                setListingIcon(listing)
                setYear(listing)
                setTotalFloors(listing)
                setFloorNumber(listing)
                setVisibilityForDetails(listing)
                initBtnEdit(listing)
                listenWhatsapp(listing)
                listenShare(listing)
                enableContact(listing)
                viewModel.userIdLiveData.value?.let {
                    binding.ivFavorite.visibleOrGone(listing.user?.id != it)
                }
                hideProgress()
                if (viewModel.viewLoadedData.value != true) {
                    //viewModel.triggerView()
                    viewModel.viewLoadedData.value = true
                }
            }
        })
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
        viewModel.userIdLiveData.observe(this, Observer { currentUserId ->
            viewModel.listingLiveData.value?.let {
                binding.ivFavorite.visibleOrGone(it.user?.id != currentUserId)
                //viewModel.triggerView()
            }
        })
        viewModel.onBackPressedLiveData.observe(this, Observer {
            handleBackAction()
        })
        viewModel.listingCreatedLiveData.observe(this, Observer {
            showSnackbar(R.string.listing_successfully_updated)
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.listingLiveData.removeObservers(this)
        viewModel.authFailedData.removeObservers(this)
        viewModel.userIdLiveData.removeObservers(this)
        viewModel.onBackPressedLiveData.removeObservers(this)
        viewModel.listingCreatedLiveData.removeObservers(this)
    }

    private fun listenWhatsapp(listing: Listing?) {
        binding.fabWhatsapp.setOnClickListener {
            listing?.let{ mListing ->
                    mListing.contacts?.first().let{
                        validateNumberPhoneWhatsapp(mListing.contacts?.first()?.contactPhone, mListing)
                    }
            }?:run{ showSnackbar(R.string.phone_number_not_available) }
        }
    }

    private fun validateNumberPhoneWhatsapp(numberPhone: String?, listing: Listing?){
        numberPhone?.let{ mPhoneNumber ->
            openWhatsapp(mPhoneNumber, listing)
            listing?.id?.let{ mId ->
                fbLogger.logEvent("Whatsapp")
                viewModel.onWhatsappClicked(mId)
            }?:run{ showSnackbar(R.string.phone_number_not_available) }
        }?:run{ showSnackbar(R.string.phone_number_not_available) }
    }

    private fun listenShare(listing: Listing?) {
        binding.shareButton.setOnClickListener {
            this.shareListingDetail(listing)
        }
    }

    private fun shareListingDetail(listing: Listing?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        val title = activity?.getString(R.string.share) ?: ""
        val message = activity?.getString(R.string.share_message) ?: ""
        val url = BuildConfig.BASE_URL_WEB + listing?.url
//        intent.putExtra(Intent.EXTRA_SUBJECT, message)
        intent.putExtra(Intent.EXTRA_TEXT, "$message $url")
        startActivity(Intent.createChooser(intent, title))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let{ mData ->
                val message = mData.extras?.getString(Constants.BUNDLE_PAYMENT_MESSAGE)?:resources.getString(R.string.listing_published_successfully)
                showSnackbar(message)
            }?:run{
                showSnackbar(R.string.listing_published_successfully)
            }
            viewModel.getListing(viewModel.displayMode)
        }
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            handleBackAction()
        }
    }

    private fun setVisibilityForDetails(listing: Listing) {
        val bedsVisible = visibilityForBeds(listing)
        val bathVisible = visibilityForBath(listing, bedsVisible)
        val parkVisible = visibilityForParking(listing, bedsVisible || bathVisible)
        visibilityForParkingForVisitors(listing, bedsVisible || bathVisible || parkVisible)
    }

    private fun handleBackAction() {
//        if (viewModel.displayMode == ListingDisplayMode.PUBLISHED ||
//            viewModel.displayMode == ListingDisplayMode.UNPUBLISHED) {
//            viewModel.navigateToListOfListings(viewModel.displayMode)
//        } else {
//            viewModel.resetListingId()
//            findNavController().navigateUp()
//        }
        viewModel.resetListingId()
        findNavController().navigateUp()
    }

    private fun updateDisplayMode(listing: Listing) {
        if (args.mode == ListingDisplayMode.IMPROPER_LISTING) {
            viewModel.displayMode = ListingDisplayMode.IMPROPER_LISTING
        } else {
            viewModel.userIdLiveData.value?.let { myUserId ->
                if (listing.user?.id == myUserId) {
                    when (listing.publishState) {
                        PublishState.NOT_PUBLISHED -> {
                            viewModel.displayMode = if (listing.isDraft) {
                                ListingDisplayMode.PREVIEW
                            } else {
                                ListingDisplayMode.UNPUBLISHED
                            }
                        }
                        PublishState.UNPUBLISHED,
                        PublishState.EXPIRED -> viewModel.displayMode = ListingDisplayMode.UNPUBLISHED
                        PublishState.PUBLISHED -> viewModel.displayMode = ListingDisplayMode.PUBLISHED
                    }
                }
                binding.mode = viewModel.displayMode
            }
        }
    }

    private fun enableContact(listing: Listing){
        Log.i("RCS-MODE", viewModel.displayMode.toString())

        when(viewModel.displayMode){
            ListingDisplayMode.PUBLISHED ->{
                binding.fabWhatsapp.gone()
                binding.btListingAction.gone()
            }
            ListingDisplayMode.UNPUBLISHED->{
                binding.fabWhatsapp.gone()
                binding.btListingAction.visible()
            }
            ListingDisplayMode.PREVIEW->{
                binding.fabWhatsapp.gone()
                binding.btListingAction.visible()
            }
            ListingDisplayMode.IMPROPER_LISTING->{
                listing.user?.id?.let{ currentUser ->
                    viewModel.userIdLiveData.value?.let { localUserId ->
                        if(localUserId == currentUser){
                            binding.fabWhatsapp.gone()
                            binding.btListingAction.gone()
                        }else{
                            binding.fabWhatsapp.visible()
                            binding.btListingAction.visible()
                        }
                    }?: run{
                        binding.fabWhatsapp.visible()
                        binding.btListingAction.visible()
                    }
                }?: run{
                    binding.fabWhatsapp.visible()
                    binding.btListingAction.visible()
                }
            }
            else ->{
                binding.fabWhatsapp.gone()
                binding.btListingAction.gone()
            }
        }
    }

    private fun visibilityForBeds(listing: Listing): Boolean {
        val isOptionVisible: Boolean
        binding.tvListingBeds.visibility =
            if (listing.bedroomsCount == null || listing.bedroomsCount == 0) {
                isOptionVisible = false
                View.GONE
            } else {
                isOptionVisible = true
                View.VISIBLE
            }

        return isOptionVisible
    }

    private fun setPlanType(listing: Listing) {
        when (listing.adPlan) {
            PaymentPlanKey.PREMIUM -> {
                ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_blue_24)
                ivPlanIcon.visible()
                tvPlanName.text = getString(
                    R.string.plan_listing,
                    PaymentPlanKey.PREMIUM.name.toLowerCase().capitalize()
                )
                tvPlanName.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.paymentPlanPremium, null)
                )
                tvPlanName.visible()
            }
            PaymentPlanKey.PLUS -> {
                ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_yellow_24)
                ivPlanIcon.visible()
                tvPlanName.text = getString(
                    R.string.plan_listing,
                    PaymentPlanKey.PLUS.name.toLowerCase().capitalize()
                )
                tvPlanName.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.paymentPlanPlus, null)
                )
                tvPlanName.visible()
            }
            else -> {
                ivPlanIcon.invisible()
                tvPlanName.invisible()
            }
        }
    }

    private fun setPublishDetails(listing: Listing) {
        if (viewModel.displayMode == ListingDisplayMode.IMPROPER_LISTING) {
            tvPublishDate.invisible()
            tvDaysLeft.invisible()
        } else {
            val adPurchasedAt = listing.adPurchasedAt
            val adExpiresAt = listing.adExpiresAt
            if (adPurchasedAt.isNullOrEmpty() ||
                adExpiresAt.isNullOrEmpty() ||
                listing.publishState == PublishState.EXPIRED
            ) {
                tvPublishDate.invisible()
                tvDaysLeft.invisible()
            } else {
                val daysLeft = DateFormatter.daysLeft(adExpiresAt)
                tvDaysLeft.text = resources.getQuantityString(
                    R.plurals.days_left_plural, daysLeft, daysLeft.toString())
                tvPublishDate.text = DateFormatter.toFullDate(adPurchasedAt)
                tvPublishDate.visible()
                tvDaysLeft.visible()
            }
        }

        setPlanType(listing)
    }

    private fun visibilityForBath(listing: Listing, addDivider: Boolean): Boolean {
        val isOptionVisible: Boolean
        binding.tvListingBath.visibility =
            if (listing.bathroomsCount == null || listing.bathroomsCount == 0) {
                isOptionVisible = false
                View.GONE
            } else {
                initDividerIcon(binding.tvListingBath, addDivider)
                isOptionVisible = true
                View.VISIBLE
            }
        return isOptionVisible
    }

    private fun visibilityForParking(listing: Listing, addDivider: Boolean): Boolean {
        val isOptionVisible: Boolean
        binding.tvListingParking.visibility =
            if (listing.parkingSlotsCount == null || listing.parkingSlotsCount == 0) {
                isOptionVisible = false
                View.GONE
            } else {
                initDividerIcon(binding.tvListingParking, addDivider)
                isOptionVisible = true
                View.VISIBLE
            }
        return isOptionVisible
    }

    private fun visibilityForParkingForVisitors(listing: Listing, addDivider: Boolean): Boolean {
        val isOptionVisible: Boolean
        binding.tvListingParkingForVisitors.visibility = if (listing.parkingForVisitors != true) {
            isOptionVisible = false
            View.GONE
        } else {
            initDividerIcon(binding.tvListingParkingForVisitors, addDivider)
            isOptionVisible = true
            View.VISIBLE
        }
        return isOptionVisible
    }

    private fun initDividerIcon(textView: TextView, isDividerVisible: Boolean) {
        textView.setCompoundDrawablesWithIntrinsicBounds(
            if (isDividerVisible) R.drawable.gray_circle else 0,
            0,
            0,
            0
        )
    }

    private fun setYear(listing: Listing) {
        binding.tvListingYear.visibility = if (listing.yearOfConstruction == null)
            View.GONE
        else
            View.VISIBLE
    }

    private fun setTotalFloors(listing: Listing) {
        binding.tvTotalFloors.visibility = if (listing.totalFloorsCount == null || listing.totalFloorsCount == 0) {
            View.GONE
        } else {
            listing.totalFloorsCount?.let {
                binding.tvTotalFloors.text = resources.getQuantityString(R.plurals.total_floors_plural, it, it)
            }
            View.VISIBLE
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun setFloorNumber(listing: Listing) {
        binding.tvFloorNumber.visibility = if (listing.floorNumber == null || listing.floorNumber == 0) {
            View.GONE
        } else {
            listing.floorNumber?.let {
                binding.tvFloorNumber.text = getString(R.string.floor_number_numb, it, it)
            }
            View.VISIBLE
        }
    }

    private fun setListingIcon(listing: Listing) {
        binding.tvListingType.setCompoundDrawablesWithIntrinsicBounds(
            ListingUtilsDelegateImpl.getListingIconByType(listing.propertyType),
            0,
            0,
            0
        )
    }

    private fun setListingType(listing: Listing) {
        val typeUpperCase = context?.let {
            ListingUtilsDelegateImpl.getLocalizedType(it, listing.listingType)
        }
        val localizedPropertyType = PropertyType.getLocalizedPropertyName(resources, listing.propertyType)
        val type = getString(R.string.property_for_sale, localizedPropertyType, typeUpperCase)
        val upperCase = type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1)
        val medium = ResourcesCompat.getFont(requireContext(), R.font.avenit_medium)
        val heavy = ResourcesCompat.getFont(requireContext(), R.font.avenir_heavy)
        val spannable = SpannableStringBuilder(upperCase)
        safeLet(medium, heavy, { medium, heavy ->
            spannable.setSpan(
                CustomTypefaceSpan("", medium),
                0,
                localizedPropertyType.lastIndex.plus(1) ?: 0,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                CustomTypefaceSpan("", heavy),
                localizedPropertyType.lastIndex.plus(2) ?: 0,
                upperCase.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            binding.tvListingType.text = spannable
        })

    }

    private fun loadMap(listing: Listing) {
        Glide.with(requireContext())
            .load(
                "https://maps.googleapis.com/maps/api/staticmap" +
                        "?center=${listing.locationAttributes?.latitude},${listing.locationAttributes?.longitude}" +
                        "&zoom=15&size=800x150&scale=2&key=${BuildConfig.PLACES_API_KEY}"
            ).transform(RoundedCorners(CORNER_RADIUS)).into(binding.tvListingMap)

        binding.tvListingMap.setOnClickListener { viewModel.navigateToFullscreenMap() }
    }

    private fun setFacilities(facilities: List<Facility>) {
        if (facilities.isEmpty()) {
            binding.tvFacilities.invisible()
        } else {
            facilitiesAdapter.add(facilities)
        }
    }

    private fun setAdvancedDetails(advancedDetails: List<Facility>) {
        if (advancedDetails.isEmpty()) {
            binding.tvAdvancedDetails.invisible()
        } else {
            advancedDetailsAdapter.add(advancedDetails)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUser(user: User?, contact: Contact?) {
        contact?.let{
            val arr = it.contactName?.split(" ")
            arr?.let{ mFullName ->
                try {
                    if(mFullName.size > 1) {
                        var name = ""
                        if(mFullName[0].isNotEmpty()) {
                            name = mFullName[0]
                        }
                        if(mFullName[1].isNotEmpty()) {
                            name = name + " " + mFullName[1][0] + "."
                        }
                        binding.tvListingUserName.text = name
//                    binding.tvListingUserName.text = "${mFullName[0]} ${mFullName[1][0]}."
                    } else binding.tvListingUserName.text = it.contactName?:""
                } catch (ex: StringIndexOutOfBoundsException){
                    print(ex.message)
                }
            }?:run{ binding.tvListingUserName.text = it.contactName?:"" }
        }?:run{
            binding.tvListingUserName.text = user?.fullName
        }
        binding.tvListingAvatar.withGlide(user?.avatar, R.drawable.ic_profile_placeholder)
    }

    private fun setImagesAdapter(listing: Listing) {
        binding.vpListingImages.adapter = ListingImagesPagerAdapter(listing.images) {
            viewModel.navigateToGallery()
        }
        binding.pagerIndicator.attachToPager(binding.vpListingImages)
        binding.tvListingImagesCount.text = listing.images?.size.toString()
    }

    private fun setPetFriendly(listing: Listing) {
        binding.tvPetFriendly.visibleOrGone(listing.petFriendly ?: false)
    }


    private fun setShowMore() {
        if (binding.tvListingDescription.maxLines != MAX_LINES) {
            binding.tvListingDescription.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (binding.tvListingDescription.lineCount > MAX_LINES) {
                            binding.tvListingDescription.maxLines = MAX_LINES
                            binding.tvListingShowMore.visible()
                        } else {
                            binding.tvListingShowMore.invisible()
                        }
                        binding.tvListingDescription.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
        }
    }

    private fun setRecycler() {
        binding.rcFacilitiesContainer.adapter = facilitiesAdapter
        binding.rvAdvancedDetailsContainer.adapter = advancedDetailsAdapter
    }

    private fun startPaymentFlow(listing: Listing) {
        val paymentIntent = Intent(context, PaymentActivity::class.java)
        paymentIntent.putExtra(PaymentActivity.EXTRA_USER_ID, listing.user?.id)
        paymentIntent.putExtra(PaymentActivity.EXTRA_LISTING_ID, listing.id)
        paymentIntent.putExtra(PaymentActivity.EXTRA_PUBLISHER_ROLE, listing.publisherRole)
        startActivityForResult(paymentIntent, PAYMENT_ACTIVITY_REQUEST_CODE)
    }

    private fun showPublishConfirmationDialog(listing: Listing) {
        context?.let {
            StyledAlertDialog.Builder(it)
                .setBodyText(getString(R.string.publish_confirmation_body))
                .setLeftButton(getString(R.string.cancel))
                .setRightButton(getString(R.string.publish), R.color.colorAccent) {
                    listing.status = Constants.VISIBLE
                    viewModel.updateListing(listing)
                }
                .build()
                .show()
        }
    }

    private fun showUnpublishConfirmationDialog(listing: Listing) {
        context?.let {
            StyledAlertDialog.Builder(it)
                .setTitleText(getString(R.string.unpublish_confirmation_title))
                .setBodyText(getString(R.string.unpublish_confirmation_body))
                .setLeftButton(getString(R.string.cancel))
                .setRightButton(getString(R.string.unpublish), R.color.colorAccent) {
                    listing.status = Constants.HIDDEN
                    viewModel.updateListing(listing)
                }
                .build()
                .show()
        }
    }

    private fun setDisplayMode(listing: Listing) {
        when (viewModel.displayMode) {
            ListingDisplayMode.PREVIEW -> {
                binding.tvListingTitle.text = getString(R.string.preview)
                binding.tvListingSubtitle.invisible()
                binding.tvListingActionText.text = getString(R.string.save)
                binding.btListingAction.setOnClickListener {
                    viewModel.createListing(false)
                }
                binding.btListingAction.visible()
            }
            ListingDisplayMode.UNPUBLISHED -> {
                binding.tvListingTitle.text = getString(R.string.preview)
                binding.tvListingSubtitle.text =
                    if (listing.publishState == PublishState.NOT_PUBLISHED) {
                        getString(R.string.not_published_listing)
                    } else {
                        getString(R.string.unpublished_listing)
                    }
                binding.tvListingSubtitle.visible()
                binding.tvListingActionText.text = getString(R.string.publish)

                binding.btListingAction.setOnClickListener {

                    if (listing.publishState == PublishState.UNPUBLISHED) {
                        when(listing.publisherRole?.trim()){
                            PUBLISHER_ROLE_REALTOR -> {
                                showSnackbar(R.string.action_must_be_done_from_web)
                            }
                            else -> {
                                showPublishConfirmationDialog(listing)
                            }
                        }
                    } else {
                        startPaymentFlow(listing)
                    }

                }
                binding.btListingAction.visible()
            }
            ListingDisplayMode.IMPROPER_LISTING -> {
                binding.tvListingActionText.text = getString(R.string.contact)

                listing.contacts?.first().let{
                    showContactDialog(it?.contactPhone)
                }?:run{
                    showContactDialog(listing.user?.phoneNumber)
                }

                if (viewModel.userIdLiveData.value != null && viewModel.userIdLiveData.value == listing.user?.id) {
                    binding.btListingAction.invisible()
                } else {
                    binding.btListingAction.visible()
                }
            }
            ListingDisplayMode.PUBLISHED -> {
                binding.tvListingTitle.text = getString(R.string.preview)
                binding.tvListingSubtitle.text = getString(R.string.published_listing)
                binding.tvListingSubtitle.visible()
                binding.btListingAction.invisible()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPriceSubtitle(it: Listing) {
        if (it.listingType == Constants.SALE) {
            binding.tvListingType.setBackgroundResource(R.drawable.listing_for_sale_shape)
            val numberFormat = NumberFormat.getInstance()
            numberFormat.maximumFractionDigits = 2
            val pricePerMeter = numberFormat.format(it.price?.toDouble()?.div(it.area ?: 0)?.toInt())
            binding.tvListingPriceMeter.text = "$$pricePerMeter/m²"
        } else {
            binding.tvListingType.setBackgroundResource(R.drawable.listing_for_rent_shape)
            binding.tvListingPriceMeter.text = getString(R.string.per_month)
        }
    }

    private fun showContactDialog(numberPhone: String?) {
        numberPhone?.let{ mNumberPhone ->
            binding.btListingAction.setOnClickListener(null)
            binding.btListingAction.setOnClickListener {
                viewModel.contactOwner()
                fbLogger.logEvent("Contactenme")
                context?.let {
                    StyledAlertDialog.Builder(it)
                        .setTitleText(mNumberPhone)
                        .setRightButton(getString(R.string.call)) {
                            navigateToCallScreen(mNumberPhone)
                        }
                        .setLeftButton(getString(R.string.cancel))
                        .build()
                        .show()
                }
            }
        }?:run{ showSnackbar(R.string.phone_number_not_available) }
    }

    private fun navigateToCallScreen(phoneNumber: String) {
        startActivity(Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
        })
    }

    private fun initBtnEdit(listing: Listing) {
        binding.btEditListing.setOnClickListener {
            showEditDialog(listing)
        }
    }

    private fun showEditDialog(listing: Listing) {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(requireContext())
            .inflate(R.layout.manage_listing_dialog, binding.root as ViewGroup, false)
        val tvOpen = v.findViewById<TextView>(R.id.tvOpen)
        val tvEdit = v.findViewById<TextView>(R.id.tvEdit)
        val tvShare = v.findViewById<TextView>(R.id.tvShare)
        val tvUnpublish = v.findViewById<TextView>(R.id.tvUnpublish)
        val tvPublish = v.findViewById<TextView>(R.id.tvPublish)
        tvOpen.invisible()
        tvShare.invisible()
        tvEdit.visible()
        if (listing.publishState == PublishState.PUBLISHED) {
            tvUnpublish.visible()
        } else if (listing.publishState != null && listing.isDraft.not()) {
            tvPublish.visible()
        }
        tvEdit.setOnClickListener {
            viewModel.navigateToEdit()
            dialog.dismiss()
        }
        tvUnpublish.setOnClickListener {

            when(listing.publisherRole?.trim()){
                PUBLISHER_ROLE_REALTOR -> {
                    showUnpublishConfirmationDialog(listing)
                    //showSnackbar(R.string.action_must_be_done_from_web)
                }
                PUBLISHER_ROLE_OWNER -> {
                    showUnpublishConfirmationDialog(listing)
                }
                else -> {
                    showUnpublishConfirmationDialog(listing)
                }
            }
            dialog.dismiss()

        }
        tvPublish.setOnClickListener {
            if (listing.publishState == PublishState.UNPUBLISHED) {
                when(listing.publisherRole?.trim()){
                    PUBLISHER_ROLE_REALTOR -> {
                        showSnackbar(R.string.action_must_be_done_from_web)
                    }
                    else -> {
                        showPublishConfirmationDialog(listing)
                    }
                }
            } else {
                startPaymentFlow(listing)
            }
            dialog.dismiss()
        }
        dialog.setContentView(v)
        dialog.show()
    }

    private fun createProgressDialog() {
        context?.let {
            progressDialog = AlertDialog.Builder(it)
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .create()
                .apply {
                    window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(it, R.color.transparent)))
                }
        }
    }

    private fun showProgress() {
        if (progressDialog == null) {
            createProgressDialog()
        }
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    private fun openWhatsapp(numberPhone: String, listing: Listing?) {
        try {
            val message = activity?.getString(R.string.whatsapp_message) ?: ""
            val urlNotice = BuildConfig.BASE_URL_WEB + listing?.url
            val url = "https://wa.me/$numberPhone?text=$message $urlNotice"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }catch (e: Exception){
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            showSnackbar(R.string.whatsapp_not_installed)
        }
    }

    companion object {
        private const val PAYMENT_ACTIVITY_REQUEST_CODE = 9856
    }
}
