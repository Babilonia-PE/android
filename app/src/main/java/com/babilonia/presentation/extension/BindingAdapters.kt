package com.babilonia.presentation.extension

import android.text.TextUtils
import android.util.Patterns
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.babilonia.R
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
}