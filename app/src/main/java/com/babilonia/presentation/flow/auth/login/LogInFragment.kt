package com.babilonia.presentation.flow.auth.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.babilonia.R
import com.babilonia.databinding.LogInFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.utils.NetworkUtil
import com.google.android.material.textfield.TextInputEditText

class LogInFragment : BaseFragment<LogInFragmentBinding, LogInViewModel>() {
    private var progressDialog: AlertDialog? = null

    override fun viewCreated() {
        binding.model = viewModel
        setErrorListeners()

        viewModel.logInLiveData.value?.ipa = NetworkUtil.getIPAddress(requireContext()) ?: ""

        binding.etPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eyes_slash, 0)
        binding.etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.etPassword.right - binding.etPassword.compoundDrawables[2].bounds.width())) {
                    togglePasswordVisibility(binding.etPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: TextInputEditText) {
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eyes, 0)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eyes_slash, 0)
        }
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.navigateToRootLiveData.observe(this, Observer {
            val intent = Intent(activity?.applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            activity?.finish()
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.logInLiveData.removeObservers(this)
        viewModel.navigateToRootLiveData.removeObservers(this)
    }

    private fun setErrorListeners() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    binding.etEmail.text?.isEmpty() == true -> binding.tyEmail.error = null
                    Patterns.EMAIL_ADDRESS.matcher(s).matches().not() -> binding.tyEmail.error =
                        getString(R.string.invalid_email)
                    else -> binding.tyEmail.error = null
                }
                //viewModel.userLiveData.value?.email = s?.toString()
                viewModel.logInValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        /*binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tyEmail.error = null
            } else if (binding.etEmail.text.isNullOrEmpty()) {
                binding.tyEmail.error = getString(R.string.field_should_not_be_empty)
            }
        }*/

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyPassword.error = getString(R.string.password_empty)
                } else {
                    binding.tyPassword.error = null
                }
                //viewModel.userLiveData.value?.password = s?.toString()
                viewModel.logInValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
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

    private fun createProgressDialog() {
        context?.let {
            progressDialog = AlertDialog.Builder(it)
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .create()
                .apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
        }
    }
}