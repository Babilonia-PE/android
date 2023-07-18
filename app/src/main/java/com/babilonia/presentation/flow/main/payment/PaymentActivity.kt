package com.babilonia.presentation.flow.main.payment

import android.os.Bundle
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity

class PaymentActivity : BaseActivity<PaymentActivitySharedViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        intent?.extras?.getLong(EXTRA_LISTING_ID)?.let { listingId ->
            viewModel.listingId = listingId
        }
    }

    override fun startListenToEvents() {
        // do nothing
    }

    override fun stopListenToEvents() {
        // do nothing
    }

    companion object {
        const val EXTRA_LISTING_ID = "listing_id"
    }
}