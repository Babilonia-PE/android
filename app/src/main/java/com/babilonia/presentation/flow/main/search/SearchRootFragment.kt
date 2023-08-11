package com.babilonia.presentation.flow.main.search

import android.content.res.ColorStateList
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.SearchRootFragmentBinding
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.RecentSearch
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.hideKeyboard
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.pxValue
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.search.common.SearchAdapter
import com.babilonia.presentation.flow.main.search.model.DisplaybleFilter
import com.babilonia.presentation.flow.main.search.model.PrefixedDisplayableFilter
import com.babilonia.presentation.utils.NetworkUtil
import com.babilonia.presentation.utils.SvgUtil.convertRecentSearch
import com.google.android.material.chip.Chip
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.search_root_fragment.*
import java.util.*

const val RECENT_SEARCH_DROPDOWN_DELAY_MS = 250L

class SearchRootFragment : BaseFragment<SearchRootFragmentBinding, ListingSearchViewModel>() {

    private val searchAdapter = SearchAdapter()
    private var skipNextSuggestion = false

    override fun viewCreated() {
        binding.model = viewModel
        binding.etSearch.setAdapter(searchAdapter)
        setSearchListeners()
        binding.filtersGroup.isGone = viewModel.hasFilters().not()
        setFiltersButton()
    }

    private fun setFiltersButton() {
        if (viewModel.hasFilters()) {
            binding.ivFilters.setImageResource(R.drawable.ic_has_filters)
        } else {
            binding.ivFilters.setImageResource(R.drawable.ic_filter_black_24)
        }
        viewModel.ipAddress = NetworkUtil.getIPAddress(requireContext()) ?: ""
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.suggestions.observe(this, Observer {
            when {
                skipNextSuggestion -> skipNextSuggestion = false
                it.isEmpty() -> searchAdapter.setNotFoundPlace(etSearch.text.toString())
                it.isNotEmpty() -> searchAdapter.setPlaces(it) }

        })
        viewModel.listings.observe(this, Observer {
            binding.executePendingBindings()
        })
        viewModel.currentPlace.observe(this, Observer {
            binding.etSearch.setText(it.toString())
            binding.etSearch.clearFocus()
        })
        viewModel.filtersLiveData.observe(this, Observer {
            initFilters(it)
        })
        viewModel.onFocusChangeEvent.observe(this, Observer {
            if (it.not()) {
                binding.etSearch.clearFocus()
                hideKeyboard()
            }
        })

        viewModel.recentSearches.observe(this, Observer {
            searchAdapter.setRecentSearches(it)
        })
        viewModel.clearSearchFromMapLiveData.observe(this, Observer {
            if(it) {
                binding.ibClearSearch.isInvisible = true
                binding.etSearch.text.clear()
                binding.etSearch.clearFocus()
                hideKeyboard()
            }
        })
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.suggestions.removeObservers(this)
        viewModel.currentPlace.removeObservers(this)
        viewModel.filtersLiveData.removeObservers(this)
        viewModel.onFocusChangeEvent.removeObservers(this)
        viewModel.listings.removeObservers(this)
        viewModel.recentSearches.removeObservers(this)
        viewModel.clearSearchFromMapLiveData.removeObservers(this)
        viewModel.authFailedData.removeObservers(this)
    }

    private fun changeSearchSize(
        right: Int,
        left: Int,
        isClearInvisible: Boolean,
        backgroundDrawable: Int,
        searchDrawable: Int? = R.drawable.ic_search_grey
    ) {
        binding.ibClearSearch.isInvisible = isClearInvisible
        binding.logo.isInvisible = isClearInvisible.not()
        binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
            searchDrawable?.let {
                ContextCompat.getDrawable(
                    requireContext(),
                    it
                )
            }, null, null, null
        )
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.ccRoot)
        constraintSet.connect(
            R.id.etSearch,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            resources.getDimension(left).toInt()
        )
        constraintSet.connect(
            R.id.etSearch,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            resources.getDimension(right).toInt()
        )
        constraintSet.connect(
            R.id.etSearch,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            resources.getDimension(R.dimen.small_margin).toInt()
        )
        constraintSet.applyTo(binding.ccRoot)
        binding.etSearch.background = ContextCompat.getDrawable(requireContext(), backgroundDrawable)
        TransitionManager.beginDelayedTransition(binding.ccRoot)
        binding.etSearch.threshold = 2

    }

    private fun initFilters(filters: List<DisplaybleFilter>) {
        binding.cbFilters.removeAllViews()
        filters.forEach { filter ->
            if (filter.value.isNotEmpty()) {
                binding.cbFilters.addView(Chip(requireContext())
                    .apply {
                        setOnClickListener {
                            binding.cbFilters.removeView(it)
                            removeFilterAndRefresh(filter, filters)
                        }

                        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCorners(
                            CornerFamily.ROUNDED,
                            4f.pxValue(context = requireContext())
                        ).build()

                        setTextAppearanceResource(R.style.ChipTextAppearance)

                        text = if (filter is PrefixedDisplayableFilter) {
                            createSpannablePrefixedText(filter)
                        } else {
                            filter.value
                        }

                        chipBackgroundColor =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    filter.backgroundColor
                                )
                            )

                        isClickable = filter.clickable
                        setTextColor(ContextCompat.getColor(requireContext(), filter.textColor))
                    })
            }
        }

        val realFiltersCount = filters.filter { it.value.isNotEmpty() }.size

        if (realFiltersCount > 1) {
            binding.tvFiltersHint.visible()
        } else {
            // In this case we have only Sale/Rent filter. It can not be removed by tap so we hide
            // 'Tap on filter to remove' hint
            binding.tvFiltersHint.invisible()
        }
    }

    private fun createSpannablePrefixedText(filter: PrefixedDisplayableFilter): Spannable? {
        return context?.let { context ->
            SpannableStringBuilder()
                .append(filter.prefixValue)
                .append(filter.value)
                .toSpannable().apply {
                    setSpan(
                        RelativeSizeSpan(0.8f),
                        0,
                        filter.prefixValue.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, filter.prefixTextColor)),
                        0,
                        filter.prefixValue.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
    }

    private fun removeFilterAndRefresh(
        filterToDelete: DisplaybleFilter,
        filters: List<DisplaybleFilter>
    ) {
        val newFilters = filters.toMutableList()
        newFilters.remove(filterToDelete)
        viewModel.filtersLiveData.value = newFilters

        viewModel.removeFilter(filterToDelete.type)
        viewModel.metadataUpdateSubject.onNext(true)
        viewModel.needToResetPaginator = true
        viewModel.getListings()
    }

    private fun setSearchListeners() {
        binding.ibClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            binding.etSearch.clearFocus()
            searchAdapter.clearNotFoundPlace()
            changeSearchSize(
                R.dimen.search_bar_margin,
                R.dimen.search_bar_margin,
                true,
                R.drawable.search_drawable
            )
            hideKeyboard()
        }
        binding.etSearch.setOnFocusChangeListener { v, hasFocus ->
            viewModel.onFocusChangeEvent.postValue(hasFocus)
            if (hasFocus) {
                changeSearchSize(
                    R.dimen.small_margin,
                    R.dimen.small_margin,
                    false,
                    R.drawable.search_drawable_filled
                )
                if (searchAdapter.count > 0) {
                    // SearchView has an animation on receive focus, we need to wait some time
                    // before opening a dropdown
                    binding.etSearch.postDelayed(
                        { binding.etSearch.showDropDown() },
                        RECENT_SEARCH_DROPDOWN_DELAY_MS
                    )
                    searchAdapter.clearPlaces()
                }
                val searchText = binding.etSearch.text
                if (searchText.length > 2) {
                    viewModel.getPlaces(searchText.toString(), 1, 10)
                }
            } else {
                if (binding.etSearch.text.isEmpty()) {
                    changeSearchSize(
                        R.dimen.search_bar_margin,
                        R.dimen.search_bar_margin,
                        true,
                        R.drawable.search_drawable
                    )
                } else {
                    changeSearchSize(
                        R.dimen.search_bar_margin,
                        R.dimen.small_margin,
                        false,
                        R.drawable.search_drawable,
                        null
                    )
                }
            }
        }
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                binding.etSearch.clearFocus()
            }
            return@setOnEditorActionListener false
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: EmptyConstants.EMPTY_STRING
                if (text.length > 2) {
                    viewModel.getPlaces(text, 1, 10)
                } else {
                    searchAdapter.clearPlaces()
                }
            }

        })
        binding.etSearch.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = searchAdapter.getItem(position)
            skipNextSuggestion = true
            viewModel.needToResetPaginator = true
            viewModel.notClearSearchFromMap()
            when (clickedItem) {
                is String -> {  // 'My location' is clicked
                    viewModel.getCurrentPlace()
                }
                is Location -> { // autocomplete suggestion is clicked
                    viewModel.getLocationSelected(clickedItem, false)
                }
                is RecentSearch -> { // recent search item clicked
                    //viewModel.getPlaceById(clickedItem.placeId)
                    viewModel.getLocationSelected(convertRecentSearch(clickedItem), false)
                }
            }
            searchAdapter.clearNotFoundPlace()
            hideKeyboard()
        }
    }
}
