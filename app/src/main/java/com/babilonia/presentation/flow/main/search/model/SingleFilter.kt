package com.babilonia.presentation.flow.main.search.model

// Created by Anton Yatsenko on 24.07.2019.
class SingleFilter(
    override val value: String,
    override val type: String,
    override val backgroundColor: Int,
    override val clickable: Boolean,
    override val textColor: Int,
    override val textStyle: Int
) : DisplaybleFilter