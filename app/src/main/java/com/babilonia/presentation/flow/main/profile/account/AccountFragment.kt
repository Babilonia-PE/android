package com.babilonia.presentation.flow.main.profile.account

import com.babilonia.R
import com.babilonia.databinding.AccountFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.view.dialog.StyledAlertDialog

class AccountFragment : BaseFragment<AccountFragmentBinding, AccountViewModel>() {
    override fun viewCreated() {

        setToolbar()
        binding.tvLogOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }

    private fun showSignOutDialog() {
        context?.let {
            StyledAlertDialog.Builder(it)
                .setTitleText(getString(R.string.sing_out_title))
                .setBodyText(getString(R.string.sign_out_message))
                .setRightButton(getString(R.string.sign_out), R.color.black) {
                    viewModel.signOut(false)
                }
                .setLeftButton(getString(R.string.cancel))
                .build()
                .show()
        }
    }
}
