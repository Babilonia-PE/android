package com.babilonia.presentation.flow.main.publish.createlisting

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.babilonia.R
import com.babilonia.databinding.CreateListingContainerFragmentBinding
import com.babilonia.domain.model.Contact
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.advanced.AdvancedDetailsFragment
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.common.ListingPage
import com.babilonia.presentation.flow.main.publish.createlisting.common.CreateListingPagerAdapter
import com.babilonia.presentation.flow.main.publish.details.CreateListingDetailsFragment
import com.babilonia.presentation.flow.main.publish.facilities.FacilitiesFragment
import com.babilonia.presentation.flow.main.publish.listingtype.ListingTypeFragment
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import com.babilonia.presentation.flow.main.publish.photos.ListingPhotosFragment
import com.babilonia.presentation.view.bubblebottombar.listener.BubbleNavigationChangeListener
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateListingContainerFragment :
    BaseCreateListingFragment<CreateListingContainerFragmentBinding, CreateListingContainerViewModel>(),
    View.OnClickListener {

    private val args: CreateListingContainerFragmentArgs by navArgs()
    private var shouldSaveAsDraft = true
    override fun onAttach(context: Context) {
        super.onAttach(context)
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.mode = args.mode

 //       binding.viewModelContainer = viewModel
 //       binding.lifecycleOwner = this.viewLifecycleOwner
 //       binding.executePendingBindings()

        when (args.mode) {
            NewListingOpenMode.DRAFT -> viewModel.getListingsById(args.id)
            NewListingOpenMode.EDIT -> viewModel.getListingsById(args.id, ListingDisplayMode.UNPUBLISHED)
            else -> sharedViewModel.fillWithDefaultValues()
        }
    }

    override fun viewCreated() {
        initToolbar()
        initViewPager()
        initClicks()
        setupPageChanges()
        binding.model = sharedViewModel

        binding.viewModelContainer = viewModel

        setOnBackPressedDispatcher()
    }

    override fun onDestroy() {
        if (shouldSaveAsDraft && args.mode != NewListingOpenMode.EDIT) {
            viewModel.createListing(sharedViewModel.mapLiveDataToParams())
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        shouldSaveAsDraft = true

        viewModel.userLiveData.observe(this, Observer {
            sharedViewModel.contact.value = Contact(it.fullName, it.email, it.phoneNumber)
        })
    }

    override fun onPause() {
        super.onPause()

        viewModel.userLiveData.removeObservers(this)
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback {
            binding.vpCreateListingContainer.requestFocus()
            val canScrollLeft = binding.vpCreateListingContainer.currentItem > 0
            if (canScrollLeft) {
                binding.vpCreateListingContainer.currentItem -= 1
                sharedViewModel.currentItem = binding.vpCreateListingContainer.currentItem
            } else {
                showSaveDraftDialog()
            }
        }
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        sharedViewModel.mediator.observe(this, Observer {
            sharedViewModel.validatePage()
        })
        viewModel.loadingEvent.observe(this, Observer {
            if (it) {
                binding.btContinue.isEnabled = false
                binding.btContinue.showProgress {
                    buttonTextRes = R.string.uploading
                    progressColor = Color.WHITE
                }
            } else {
                binding.btContinue.isEnabled = true
                val position = binding.vpCreateListingContainer.currentItem
                binding.btContinue.hideProgress(
                    when {
                        position < NUMBER_OF_PAGES - 1 -> {
                            getString(R.string.continue_creating, position + 1, NUMBER_OF_PAGES)
                        }
                        args.mode == NewListingOpenMode.EDIT -> getString(R.string.save)
                        else -> getString(R.string.preview)
                    }
                )
            }
        })
        viewModel.gotListingEvent.observe(this, Observer {
            sharedViewModel.status = it.status
            sharedViewModel.setDraft(it, args.mode)
            it.contacts?.first()?.let { contact ->
                if (contact.contactName == null || contact.contactEmail == null || contact.contactPhone == null)
                    viewModel.getUser()
            }
        })
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
        viewModel.disableComponentsEvent.observe(this, Observer {
            binding.btContinue.isEnabled = !it
            binding.statusView.visibleOrGone(!it)
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        sharedViewModel.mediator.removeObservers(this)
        viewModel.imageUploadedEvent.removeObservers(this)
        viewModel.loadingEvent.removeObservers(this)
        viewModel.gotListingEvent.removeObservers(this)
        viewModel.authFailedData.removeObservers(this)
        viewModel.disableComponentsEvent.removeObservers(this)
    }

    override fun onClick(v: View) {
        if (binding.statusView.getItemPositionById(v.id) < binding.vpCreateListingContainer.currentItem) {
            binding.statusView.onBubbleClick(v)
        }
    }

    private fun initClicks() {
        bindProgressButton(binding.btContinue)
        binding.btContinue.setOnClickListener {
            binding.vpCreateListingContainer.requestFocus()
            val canScrollRight = binding.vpCreateListingContainer.currentItem < NUMBER_OF_PAGES - 1
            if (canScrollRight) {
                binding.vpCreateListingContainer.currentItem += 1
                sharedViewModel.currentItem = binding.vpCreateListingContainer.currentItem
            } else {
                val params = sharedViewModel.mapLiveDataToParams()
                if (args.mode == NewListingOpenMode.EDIT) {
                    params.status = null
                    viewModel.updateListing(params)
                } else {
                    shouldSaveAsDraft = false
                    viewModel.createListing(params, true)
                }
            }
        }
    }

    private fun initToolbar() {
        if (args.mode == NewListingOpenMode.EDIT) {
            binding.toolbar.title = requireContext().getString(R.string.edit_listing)
        }
        binding.tvExit.setOnClickListener {
            showSaveDraftDialog()

        }
    }

    private fun initViewPager() {
        val adapter = CreateListingPagerAdapter(this)
        adapter.add(ListingTypeFragment.newInstance())
        adapter.add(CreateListingDetailsFragment.newInstance())
        adapter.add(FacilitiesFragment.newInstance())
        adapter.add(AdvancedDetailsFragment.newInstance())
        adapter.add(ListingPhotosFragment.newInstance())
        binding.vpCreateListingContainer.adapter = adapter
        binding.vpCreateListingContainer.isUserInputEnabled = false
    }

    private fun setPageAndValidate(position: Int) {
        sharedViewModel.currentPage = ListingPage.byPage(position)
        sharedViewModel.validatePage()
        binding.btContinue.text =
            when {
                position < NUMBER_OF_PAGES - 1 -> getString(R.string.continue_creating, position + 1, NUMBER_OF_PAGES)
                args.mode == NewListingOpenMode.EDIT -> getString(R.string.save)
                else -> getString(R.string.preview)
            }
    }

    private fun setupPageChanges() {
        binding.btContinue.text = getString(R.string.continue_creating, 1, NUMBER_OF_PAGES)
        binding.vpCreateListingContainer.offscreenPageLimit = 4

        binding.cCommon.setOnClickListener(this)
        binding.cDetails.setOnClickListener(this)
        binding.cFacilities.setOnClickListener(this)
        binding.cAdvancedDetails.setOnClickListener(this)
        binding.cPhotos.setOnClickListener(this)

        binding.statusView.setCurrentActiveItem(binding.vpCreateListingContainer.currentItem)
        binding.vpCreateListingContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                binding.statusView.setCurrentActiveItem(position)
                setPageAndValidate(position)
            }
        })
        binding.statusView.setNavigationChangeListener(object : BubbleNavigationChangeListener {
            override fun onNavigationChanged(view: View, position: Int) {
                binding.vpCreateListingContainer.setCurrentItem(position, true)
                sharedViewModel.currentItem = position
            }
        })
    }

    private fun showSaveDraftDialog() {
        val bodyText = if (args.mode == NewListingOpenMode.EDIT) {
            getString(R.string.finish_edit_listing)
        } else {
            getString(R.string.finish_edit_description)
        }
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.materialDialogStyle)
            .setTitle(getString(R.string.finish_edit))
            .setMessage(bodyText)
        if (args.mode != NewListingOpenMode.EDIT) {
            dialogBuilder
                .setNeutralButton(getString(R.string.save_and_exit)) { _, _ ->
                    val params = sharedViewModel.mapLiveDataToParams()
                    shouldSaveAsDraft = false
                    viewModel.createListing(params, shouldNavigateBack = true)

                }
        }
        dialogBuilder.setNegativeButton(getString(R.string.continute_creating)) { dialog, _ ->
            dialog.dismiss()
        }.setPositiveButton(getString(R.string.dont_save_and_exit)) { dialog, _ ->
            shouldSaveAsDraft = false
            dialog.dismiss()
            viewModel.navigateBack()
        }.show()
    }

    companion object {
        private const val NUMBER_OF_PAGES = 5
    }
}
