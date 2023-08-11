package com.babilonia.presentation.flow.main.profile.account

import com.babilonia.R
import com.babilonia.databinding.AccountFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import androidx.lifecycle.Observer

class AccountFragment : BaseFragment<AccountFragmentBinding, AccountViewModel>() {
    override fun viewCreated() {

        setToolbar()
        binding.tvLogOut.setOnClickListener {
            showSignOutDialog()
        }
        binding.tvDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }

    override fun startListenToEvents() {
        super.startListenToEvents()

        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.authFailedData.removeObservers(this)
    }

    private fun showSignOutDialog() {
        context?.let { _context ->
            StyledAlertDialog.Builder(_context)
                .setTitleText(getString(R.string.sing_out_title))
                .setBodyText(getString(R.string.sign_out_message))
                .setRightButton(getString(R.string.sign_out), R.color.black) {
                    viewModel.signOut(false, _context)
                }
                .setLeftButton(getString(R.string.cancel))
                .build()
                .show()
        }
    }

    private fun showDeleteAccountDialog() {
        context?.let { _context ->
            StyledAlertDialog.Builder(_context)
                .setTitleText(getString(R.string.delete_account_title))
                .setBodyText(getString(R.string.delete_account_message))
                .setRightButton(getString(R.string.delete_account), R.color.black) {
                    viewModel.deleteAccount(_context)
                }
                .setLeftButton(getString(R.string.cancel))
                .build()
                .show()
        }
    }
}
