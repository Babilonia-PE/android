package com.babilonia.presentation.flow.main.search.model

class PrefixedDisplayableFilter(override val value: String,
                                override val type: String,
                                override val backgroundColor: Int,
                                override val clickable: Boolean,
                                override val textColor: Int,
                                override val textStyle: Int,
                                val prefixValue: String,
                                val prefixTextColor: Int
) : DisplaybleFilter