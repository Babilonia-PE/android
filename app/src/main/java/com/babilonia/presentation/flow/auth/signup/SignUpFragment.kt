package com.babilonia.presentation.flow.auth.signup

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import com.babilonia.R
import com.babilonia.databinding.SignUpFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import androidx.lifecycle.Observer
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.utils.NetworkUtil

class SignUpFragment : BaseFragment<SignUpFragmentBinding, SignUpViewModel>() {
    private var progressDialog: AlertDialog? = null

    override fun viewCreated() {
        binding.model = viewModel
        setErrorListeners()

        viewModel.signUpLiveData.value?.ipa = NetworkUtil.getIPAddress(requireContext()) ?: ""

        //val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //val country = tm.simCountryIso
        //binding.tyPhone.setHint(R.string.your_phone_number)
        //binding.tyPhone.setDefaultCountry(country);
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.emailAlreadyTakenLiveData.observe(this, Observer {
            binding.tyEmail.error = getString(R.string.email_already_taken)
            binding.btChange.apply {
                isEnabled = false
                alpha = 0.5f
            }
        })
        viewModel.navigateToRootLiveData.observe(this, Observer {
            val intent = Intent(activity?.applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            activity?.finish()
        })
        viewModel.waitingLiveData.observe(this, Observer {
            if (it) {
                showProgress()
            } else {
                hideProgress()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.emailAlreadyTakenLiveData.removeObservers(this)
        viewModel.signUpLiveData.removeObservers(this)
        viewModel.navigateToRootLiveData.removeObservers(this)
        viewModel.waitingLiveData.removeObservers(this)
    }

    private fun setErrorListeners() {
        binding.etFullName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyFullName.error = getString(R.string.first_name_empty)
                } else {
                    binding.tyFullName.error = null
                }
                viewModel.signUpValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    binding.etEmail.text?.isEmpty() == true -> binding.tyEmail.error = null
                    Patterns.EMAIL_ADDRESS.matcher(s).matches().not() -> binding.tyEmail.error =
                        getString(R.string.invalid_email)
                    else -> binding.tyEmail.error = null
                }
                viewModel.signUpValidator.notifyChange()
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
                viewModel.signUpValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                /*val phoneNumber = s.toString()
                var phonePrefix = ""
                val ind = binding.tyPhone.phoneNumber.indexOf(phoneNumber)
                if (ind >= 0) {
                    phonePrefix = binding.tyPhone.phoneNumber.substring(0, ind)
                }

                viewModel.signUpLiveData.value?.phoneNumber = phoneNumber
                viewModel.signUpLiveData.value?.phonePrefix = phonePrefix*/

                if (!viewModel.isValidPhone()) {
                    binding.tyPhone.error = getString(R.string.invalid_phonenumber)
                } else {
                    binding.tyPhone.error = null
                }

                //viewModel.signUpLiveData.value?.validPhone = binding.tyPhone.isValid
                viewModel.signUpValidator.notifyChange()
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