package com.babilonia.presentation.flow.auth.splash

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import com.babilonia.R
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.openPlayStore
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import timber.log.Timber


private const val APP_REQUEST_CODE = 99

class SplashFragment : BaseFragment<com.babilonia.databinding.SplashFragmentBinding, SplashViewModel>() {

    override fun onResume() {
        super.onResume()
        checkDeepLinks()
    }

    override fun viewCreated() {
        setupClicks()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                user?.getIdToken(true)?.addOnSuccessListener {  tokenResult ->
                    tokenResult.token?.let { viewModel.loginWithFirebaseCode(it) }
                }
            }
        }
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.isLoggedInEvent.observe(this, Observer {
            binding.btSignUp.visible()
            binding.btLogin.visible()
        })
        viewModel.navigateToRootLiveData.observe(this, Observer {
            val intent = Intent(activity?.applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            activity?.finish()
        })
        viewModel.navigateToPlayStore.observe(this, Observer {
            showNewVersionDialog()
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.isLoggedInEvent.removeObservers(this)
        viewModel.navigateToRootLiveData.removeObservers(this)
        viewModel.navigateToPlayStore.removeObservers(this)
    }

    private fun setupClicks() {
        binding.btSignUp.setOnClickListener {
            startSignUp()
        }
        binding.btLogin.setOnClickListener {
            //authorizeWithPhone()
            startLogIn()
        }
        binding.ivBabilonia.setOnClickListener {
            //force crash throw RuntimeException("crash testing production")
        }
    }

    private fun startSignUp() {
        viewModel.navigateToSignUp()
    }

    private fun startLogIn() {
        viewModel.navigateToLogIn()
    }

    /*private fun authorizeWithPhone() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.FirebaseUI)
                .setAvailableProviders(providers)
                //.setIsSmartLockEnabled(false)
                .build(),
            APP_REQUEST_CODE)
    }*/

    private fun checkDeepLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(requireActivity().intent)
            .addOnSuccessListener(requireActivity()) { pendingDynamicLinkData ->
                val link = pendingDynamicLinkData?.link
                link?.let {
                    viewModel.onNewDeeplink(it)
                }
                if (activity?.isTaskRoot == true || link != null) {
                    viewModel.initAppConfig()
                } else {
                    // this is workaround for case when app was minimized and app icon is clicked
                    // (when app is resumed not from task manager but from shortcut)
                    activity?.finish()
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Timber.e(e)
                if (activity?.isTaskRoot == true) {
                    viewModel.initAppConfig()
                } else {
                    activity?.finish()
                }
            }
    }

    private fun showNewVersionDialog() {
        context?.let { _context ->
            StyledAlertDialog.Builder(_context)
                .setTitleText(getString(R.string.forceUpdate_popUp_title))
                .setBodyText(getString(R.string.forceUpdate_popUp_body))
                .setRightButton(getString(R.string.forceUpdate_popUp_buttonText), R.color.black) {
                    _context.openPlayStore()
                }
                .build()
                .show()
        }
    }
}
