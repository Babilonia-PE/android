package com.babilonia.presentation.extension

import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.babilonia.R
import com.babilonia.domain.model.Location
import com.babilonia.presentation.App
import com.babilonia.presentation.utils.SvgUtil.concatString
import com.babilonia.presentation.utils.SvgUtil.refactorAddress
import com.babilonia.presentation.utils.SvgUtil.updateProvince
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


object BindingAdapters {
    @JvmStatic
    @BindingAdapter("app:url")
    fun setImageUrl(view: ImageView, url: String?) {
        view.withGlide(url, R.drawable.ic_profile_placeholder)
    }

    @JvmStatic
    @BindingAdapter("app:emailValidator")
    fun passwordValidator(editText: TextInputLayout, email: String?) {
        // ignore infinite loops
        if (TextUtils.isEmpty(email)) {
            editText.error = null
            return
        }
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.error = null
        } else {
            editText.error = editText.context.getString(R.string.invalid_email)
        }
    }

    @JvmStatic
    @BindingAdapter("app:validateString")
    fun validateString(editText: TextInputEditText, text: String?) {
        text?.let{
            editText.setText(it)
        }?: run {
            editText.setText("")
        }
    }

    @JvmStatic
    @BindingAdapter("app:validateInputString")
    fun validateInputString(editText: TextInputLayout, text: String?) {
        if(text.isNullOrBlank()){
            editText.visibility = View.GONE
        }else{
            editText.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("app:validateAddressString")
    fun validateAddressString(editText: TextInputLayout, text: String?) {
        if(text.isNullOrBlank()){
            editText.visibility = View.GONE
        }else{
            if(text.trim() == App.applicationContext().getString(R.string.unnamed_road).trim())
                editText.visibility = View.GONE
            else editText.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("app:validateLocationString")
    fun validateLocationString(appCompatTextView: AppCompatTextView, locationAttributes: Location?) {
        val address    = refactorAddress(locationAttributes?.address)
        //val department = updateDepartment(locationAttributes?.department)
        val province   = updateProvince(locationAttributes?.province)
        val district   = updateProvince(locationAttributes?.district)

        val txt = concatString(address, district, province, "")
        if(txt.isBlank()){
            appCompatTextView.visibility = View.GONE
        }else{
            appCompatTextView.visibility = View.VISIBLE
            appCompatTextView.text = txt
        }
    }

    @JvmStatic
    @BindingAdapter("app:valLocationString")
    fun valLocationString(textView: TextView, locationAttributes: Location?) {
        val address    = refactorAddress(locationAttributes?.address)
        //val department = updateDepartment(locationAttributes?.department)
        val province   = updateProvince(locationAttributes?.province)
        val district   = updateProvince(locationAttributes?.district)

        val txt = concatString(address, district, province, "")
        if(txt.isBlank()){
            textView.visibility = View.GONE
        }else{
            textView.visibility = View.VISIBLE
            textView.text = txt
        }
    }

    @JvmStatic
    @BindingAdapter("setEnableTextInputEditText")
    fun setEnableTextInputEditText(view: TextInputEditText, value: Boolean) {
        view.isEnabled = value
    }
}