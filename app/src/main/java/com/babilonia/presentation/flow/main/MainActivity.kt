package com.babilonia.presentation.flow.main

import android.os.Bundle
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity
import dagger.android.AndroidInjection


class MainActivity : BaseActivity<MainActivityViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidInjection.inject(this);
    }

    override fun startListenToEvents() {

    }

    override fun stopListenToEvents() {

    }


}
