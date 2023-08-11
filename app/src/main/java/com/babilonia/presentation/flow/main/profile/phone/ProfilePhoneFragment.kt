package com.babilonia.presentation.flow.main.profile.phone

import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.babilonia.R
import com.babilonia.databinding.ProfilePhoneFragmentBinding
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.profile.ProfileViewModel

class ProfilePhoneFragment : BaseFragment<ProfilePhoneFragmentBinding, ProfileViewModel>() {
    override fun viewCreated() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        viewModel.editType = SuccessMessageType.PHONE_NUMBER
        binding.model = viewModel
        viewModel.getUser()
        setToolbar()
        setErrorListeners()
    }

    override fun onStop() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onStop()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        /*viewModel.emailAlreadyTakenLiveData.observe(this, Observer {
            binding.tyPhone.error = getString(R.string.email_already_taken)
            binding.btChange.apply {
                isEnabled = false
                alpha = 0.5f
            }
        })*/
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        //viewModel.emailAlreadyTakenLiveData.removeObservers(this)
    }

    private fun setErrorListeners() {
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    //s?.trim().isNullOrEmpty() -> binding.tyPhone.error = getString(R.string.field_should_not_be_empty)
                    s?.trim().isNullOrEmpty().not() && Patterns.PHONE.matcher(s).matches().not() -> binding.tyPhone.error =
                        getString(R.string.invalid_phonenumber)
                    else -> binding.tyPhone.error = null
                }
                viewModel.updatePhoneValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }
}