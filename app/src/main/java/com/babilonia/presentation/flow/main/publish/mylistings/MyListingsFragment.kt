package com.babilonia.presentation.flow.main.publish.mylistings

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.babilonia.BuildConfig
import com.babilonia.Constants
import com.babilonia.Constants.BUNDLE_PAYMENT_MESSAGE
import com.babilonia.Constants.PUBLISHER_ROLE_OWNER
import com.babilonia.Constants.PUBLISHER_ROLE_REALTOR
import com.babilonia.R
import com.babilonia.databinding.MyListingsFragmentBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PublishState
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.gone
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.payment.PaymentActivity
import com.babilonia.presentation.flow.main.publish.mylistings.common.MyListingsPagerAdapter
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout

class MyListingsFragment : BaseFragment<MyListingsFragmentBinding, MyListingsViewModel>() {
    private val adapter by lazy { MyListingsPagerAdapter(
        viewModel, listOf(getString(R.string.published), getString(R.string.not_published_tab_name))) }

    private val navArgs: MyListingsFragmentArgs by navArgs()

    override fun viewCreated() {
        setEmptyState()
        initViewPager()
        viewModel.getMyListings()
        checkShorCutsCall()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.myListings.observe(this, Observer { items ->
            setListings(items)
        })
        viewModel.onMoreClickedEvent.observe(this, Observer {
            showPickerDialog(it)
        })
        viewModel.onShareClickedEvent.observe(this, Observer {
            shareListingDetail(it)
        })
        viewModel.onListingUpdatedLiveData.observe(this, Observer {
            viewModel.myListings.value?.let {
                setListings(it)
            }
        })
        viewModel.onListingDeletedLiveData.observe(this, Observer {
            showSnackbar(R.string.delete_listing_success_message)
        })
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.myListings.removeObservers(this)
        viewModel.onMoreClickedEvent.removeObservers(this)
        viewModel.onShareClickedEvent.removeObservers(this)
        viewModel.onListingUpdatedLiveData.removeObservers(this)
        viewModel.onListingDeletedLiveData.removeObservers(this)
        viewModel.authFailedData.removeObservers(this)
    }

    private fun checkShorCutsCall() {
        val extraParam = Intent.EXTRA_SHORTCUT_ID
        if(activity?.intent?.hasExtra(extraParam) == true){
            val extra = activity?.intent?.getStringExtra(extraParam)
            activity?.intent?.removeExtra(extraParam)

            when (extra)  {
                "open_create_listing" ->
                    viewModel.navigateToCreateListing()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let{ mData ->
                val message = mData.extras?.getString(BUNDLE_PAYMENT_MESSAGE)?:resources.getString(R.string.listing_published_successfully)
                showSnackbar(message)
            }?:run{
                showSnackbar(R.string.listing_published_successfully)
            }
            viewModel.getMyListings()
        }
    }

    private fun initViewPager() {
        binding.vpMyListings.adapter = adapter
        binding.vpMyListings.offscreenPageLimit = 2
        binding.vpMyListings.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.tabs.selectTab(binding.tabs.getTabAt(position))
            }
        })
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.vpMyListings.currentItem = it.position
                }
            }
        })

        if (navArgs.activeTab == ListingDisplayMode.UNPUBLISHED) {
            binding.vpMyListings.setCurrentItem(NOT_PUBLISHED_TAB_INDEX, false)
        } else {
            binding.vpMyListings.setCurrentItem(PUBLISHED_TAB_INDEX, false)
        }
    }

    private fun setEmptyState() {
        binding.fabEmptyButton.setOnClickListener {
            binding.fabEmptyButton.visibility = View.INVISIBLE
            viewModel.navigateToCreateListing(mode = NewListingOpenMode.NEW)
        }
        binding.btnCreateListing.setOnClickListener {
            viewModel.navigateToCreateListing(mode = NewListingOpenMode.NEW)
        }
    }

    private fun setListings(items: List<Listing>) {
        when {
            items.isEmpty() -> binding.fabEmptyButton.hide()
            else -> binding.fabEmptyButton.show()
        }
        binding.emptyGroup.isVisible = items.isEmpty()
        binding.tabs.isVisible = items.isNotEmpty()
        binding.vpMyListings.isVisible = items.isNotEmpty()
        adapter.add(items)
    }

    override fun onResume() {
        super.onResume()
        binding.fabEmptyButton.show()
    }

    private fun showPickerDialog(listing: Listing) {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(requireContext())
            .inflate(R.layout.manage_listing_dialog, binding.root as ViewGroup, false)
        val tvOpen = v.findViewById<TextView>(R.id.tvOpen)
        val tvEdit = v.findViewById<TextView>(R.id.tvEdit)
        val tvUnpublish = v.findViewById<TextView>(R.id.tvUnpublish)
        val tvPublish = v.findViewById<TextView>(R.id.tvPublish)
        val tvDelete = v.findViewById<TextView>(R.id.tvDelete)
        val tvShare = v.findViewById<TextView>(R.id.tvShare)
        if (listing.isDraft) {
            tvDelete.visible()
        } else {
            tvEdit.visible()
        }
        if (listing.publishState == PublishState.PUBLISHED) {
            tvShare.visible()
        } else {
            tvShare.gone()
        }
        if (listing.publishState == PublishState.PUBLISHED) {
            tvUnpublish.visible()
        } else if (listing.publishState != null) {
            tvPublish.visible()
        }
        tvOpen.setOnClickListener {
            if (listing.isDraft) {
                viewModel.onDraftClicked(listing.id)
            } else {
                viewModel.onMyListingClicked(listing.id, listing.status)
            }
            dialog.dismiss()
        }
        tvShare.setOnClickListener {
            viewModel.onShareClicked(listing)
            dialog.dismiss()
        }
        tvDelete.setOnClickListener {
            showDeleteConfirmationDialog(listing)
            dialog.dismiss()
        }
        tvEdit.setOnClickListener {
            listing.id?.let { it1 -> viewModel.navigateToEdit(it1) }
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
                when(listing.publisherRole?.trim()){
                    PUBLISHER_ROLE_REALTOR -> {
                        showSnackbar(R.string.action_must_be_done_from_web)
                    }
                    else -> {
                        startPaymentFlow(listing)
                    }
                }
            }
            dialog.dismiss()
        }
        dialog.setContentView(v)
        dialog.show()
    }

    private fun shareListingDetail(listing: Listing) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        val title = activity?.getString(R.string.share) ?: ""
        val message = activity?.getString(R.string.share_message) ?: ""
        val url = BuildConfig.BASE_URL_WEB + "listings/"+id.toString()
//        intent.putExtra(Intent.EXTRA_SUBJECT, message)
        intent.putExtra(Intent.EXTRA_TEXT, "$message $url")
        startActivity(Intent.createChooser(intent, title))
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

    private fun showDeleteConfirmationDialog(listing: Listing) {
        context?.let {
            StyledAlertDialog.Builder(it)
                .setTitleText(getString(R.string.delete_confirmation_title))
                .setBodyText(getString(R.string.delete_confirmation_body))
                .setLeftButton(getString(R.string.cancel))
                .setRightButton(getString(R.string.delete)) {
                    listing.id?.let { it1 -> viewModel.deleteDraft(it1) }
                    adapter.remove(listing)
                }
                .build()
                .show()
        }
    }

    private fun startPaymentFlow(listing: Listing) {
        val paymentIntent = Intent(context, PaymentActivity::class.java)
        paymentIntent.putExtra(PaymentActivity.EXTRA_USER_ID, listing.user?.id)
        paymentIntent.putExtra(PaymentActivity.EXTRA_LISTING_ID, listing.id)
        paymentIntent.putExtra(PaymentActivity.EXTRA_PUBLISHER_ROLE, listing.publisherRole)
        startActivityForResult(paymentIntent, PAYMENT_ACTIVITY_REQUEST_CODE)
    }

    companion object {
        private const val PUBLISHED_TAB_INDEX = 0
        private const val NOT_PUBLISHED_TAB_INDEX = 1

        private const val PAYMENT_ACTIVITY_REQUEST_CODE = 9856
    }
}
