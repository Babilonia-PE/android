package com.babilonia.presentation.flow.ar

import android.os.Bundle
import com.babilonia.R
import com.babilonia.presentation.base.BaseActivity
import dagger.android.AndroidInjection

class ArSceneActivity : BaseActivity<ArSceneSharedViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_scene)
        AndroidInjection.inject(this)
    }

    override fun startListenToEvents() {
        // do nothing
    }

    override fun stopListenToEvents() {
        // do nothing
    }
}