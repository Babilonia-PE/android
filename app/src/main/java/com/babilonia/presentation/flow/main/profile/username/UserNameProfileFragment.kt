package com.babilonia.presentation.flow.main.profile.username

import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import com.babilonia.R
import com.babilonia.databinding.UserNameProfileFragmentBinding
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.profile.ProfileViewModel

class UserNameProfileFragment : BaseFragment<UserNameProfileFragmentBinding, ProfileViewModel>() {
    override fun viewCreated() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        viewModel.editType = SuccessMessageType.USERNAME
        binding.model = viewModel
        viewModel.getUser()
        setToolbar()
        setErrorListeners()
    }

    override fun onStop() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onStop()
    }

    private fun setErrorListeners() {
        binding.etFirstName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyFirstName.error = getString(R.string.first_name_empty)
                } else {
                    binding.tyFirstName.error = null
                }
                viewModel.updateUserNameValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyLastName.error = getString(R.string.last_name_empty)
                } else {
                    binding.tyLastName.error = null
                }
                viewModel.updateUserNameValidator.notifyChange()
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
