package com.babilonia.presentation.flow.auth

import android.os.Bundle
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity

class AuthActivity : BaseActivity<AuthActivityViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }

    override fun startListenToEvents() {
    }

    override fun stopListenToEvents() {
    }

}
