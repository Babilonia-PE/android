package com.babilonia.presentation.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.data.network.error.NoNetworkException
import com.babilonia.data.network.error.RefreshTokenException
import com.babilonia.data.network.error.ServerUnreachableException
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.presentation.extension.getLayoutRes
import com.babilonia.presentation.flow.auth.AuthActivity
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import java.lang.reflect.ParameterizedType
import javax.inject.Inject


// Created by Anton Yatsenko on 26.02.2019.
abstract class BaseFragment<B : ViewDataBinding, VM : BaseViewModel> : DaggerFragment() {

    val viewModel: VM by lazy { ViewModelProviders.of(requireActivity(), viewModelFactory).get(getViewModelClass()) }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var binding: B

    abstract fun viewCreated()

    override fun onResume() {
        super.onResume()
        startListenToEvents()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCreated()
        //setOnBackPressedDispatcher overrides in each viewpager fragment, but we need to override it only in Container Fragment
        if (this !is BaseCreateListingFragment) {
            setOnBackPressedDispatcher()
        }
    }

    protected open fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (findNavController().navigateUp().not()) {
                if (requireActivity() is MainActivity) {
                    // TODO find better solution
                    // By some reason, CreateListingContainerViewModel is not deleted when activity
                    // is finished (but onCleared is called inside of it). That's why
                    // GetFacilitiesUseCase inside this ViewModel is not working after you exited
                    // app by backbutton, opened it again and try to create a listing.
                    // Killing process solves the problem but this is a bad solution.
                    android.os.Process.killProcess(android.os.Process.myPid())
                } else {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopListenToEvents()
    }

    override fun onDestroy() {
        hideKeyboard()
        super.onDestroy()
    }

    protected open fun startListenToEvents() {
        viewModel.navigationCommands.observe(this, Observer {
            proceedNavigation(it)
        })
        viewModel.dataError.observe(this, Observer {
            when (it) {
                is NoNetworkException, is ServerUnreachableException -> showError(getString(R.string.no_internet_connection))
                is RefreshTokenException -> viewModel.signOut(true)
                else -> showError(it.localizedMessage)
            }

        })
        viewModel.messageEvent.observe(this, Observer { handleSuccessEvent(it) })
    }

    protected open fun stopListenToEvents() {
        viewModel.navigationCommands.removeObservers(this)
        viewModel.dataError.removeObservers(this)
        viewModel.messageEvent.removeObservers(this)
    }

    protected fun showSnackbar(message: Int) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showError(message: String?) {
        view?.let {
            Snackbar.make(it, message ?: EmptyConstants.EMPTY_STRING, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun proceedNavigation(it: NavigationCommand?) {
        when (it) {
            is NavigationCommand.Back -> {
                NavHostFragment.findNavController(this).navigateUp()
            }
            is NavigationCommand.BackTo -> {
                NavHostFragment.findNavController(this).popBackStack(it.destinationId, true)
            }
            is NavigationCommand.ToDestination -> {

                NavHostFragment.findNavController(this).navigate(it.directions)
                if (it.closePrevious) {
                    activity?.finish()
                }
            }
            is NavigationCommand.ToScreen -> {
                NavHostFragment.findNavController(this).navigate(it.destinationId, it.bundle)
            }
            is NavigationCommand.SignOut -> {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                activity?.finish()
            }
            is NavigationCommand.ToDestinationGlobal -> {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(it.directions)
                if (it.closePrevious) {
                    activity?.finish()
                }
            }
            is NavigationCommand.ToScreenGlobal -> {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(it.destinationId, it.bundle)
            }
        }
    }

    private fun hideKeyboard() {
        val view = requireActivity().findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getViewModelClass(): Class<VM> {
        val type =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1]
        return type as Class<VM>
    }

    @CallSuper
    open fun handleSuccessEvent(type: SuccessMessageType) {
        when (type) {
            SuccessMessageType.EMAIL -> showSnackbar(R.string.email_saved)
            SuccessMessageType.AVATAR -> showSnackbar(R.string.photo_uploaded)
            SuccessMessageType.USERNAME -> showSnackbar(R.string.user_updated)
        }
    }
}