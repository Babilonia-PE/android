package com.babilonia.presentation.flow.main.profile.phone

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
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
        viewModel.getListPaisPrefix()
        setToolbar()
        setListPaisPrefix()
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

    private fun setListPaisPrefix() {
        viewModel.listPaisPrefix.observe(viewLifecycleOwner) { paisPrefixList ->
            val presentationPaisPrefixList: List<PaisPrefix> =
                paisPrefixList.map { domainPaisPrefix ->
                    PaisPrefix(
                        domainPaisPrefix.name,
                        domainPaisPrefix.prefix,
                        domainPaisPrefix.mask,
                        domainPaisPrefix.isoCode
                    )
                }
            val adapter = object : ArrayAdapter<PaisPrefix>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                presentationPaisPrefixList
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val record = getItem(position)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.text = record?.isoCode
                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val record = getItem(position)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.text = record?.name
                    return view
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spPaisPrefix.adapter = adapter
            binding.spPaisPrefix.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.prefix = presentationPaisPrefixList[position].prefix
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    viewModel.prefix = "51"
                }
            }
        }
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

data class PaisPrefix(
    var name: String,
    var prefix: String,
    var mask: String,
    var isoCode: String
) {
    override fun toString(): String {
        return isoCode
    }
}
