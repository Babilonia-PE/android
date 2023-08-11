package com.babilonia.presentation.flow.main

import android.os.Bundle
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity
import dagger.android.AndroidInjection
import android.content.Intent





class MainActivity : BaseActivity<MainActivityViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidInjection.inject(this);
        viewModel.createShorCut(this)
    }

    override fun startListenToEvents() {

    }

    override fun stopListenToEvents() {

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
