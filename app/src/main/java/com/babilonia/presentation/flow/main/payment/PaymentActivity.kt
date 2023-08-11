package com.babilonia.presentation.flow.main.payment

import android.os.Bundle
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity

class PaymentActivity : BaseActivity<PaymentActivitySharedViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val bundle = intent?.extras
        bundle?.let{ mBundle ->
            viewModel.userId        = mBundle.getLong(EXTRA_USER_ID)
            viewModel.listingId     = mBundle.getLong(EXTRA_LISTING_ID)
            viewModel.publisherRole = mBundle.getString(EXTRA_PUBLISHER_ROLE)?: EmptyConstants.EMPTY_STRING
        }
    }

    override fun startListenToEvents() {
        // do nothing
    }

    override fun stopListenToEvents() {
        // do nothing
    }

    companion object {
        const val EXTRA_LISTING_ID     = "listing_id"
        const val EXTRA_PUBLISHER_ROLE = "publisher_role"
        const val EXTRA_USER_ID        = "user_id"
    }
}