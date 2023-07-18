package com.babilonia.presentation.utils

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.*


// Created by Anton Yatsenko on 07.06.2019.
class NumberTextWatcher(private val et: EditText, pattern: String) : TextWatcher {

    private val df: DecimalFormat
    private val dfnd: DecimalFormat
    private var hasFractionalPart: Boolean = false
    private var trailingZeroCount: Int = 0

    init {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        df = DecimalFormat(pattern, decimalFormatSymbols)
        df.isDecimalSeparatorAlwaysShown = true
        dfnd = DecimalFormat("#,###", decimalFormatSymbols)
        hasFractionalPart = false
    }

    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(s: Editable?) {
        et.removeTextChangedListener(this)

        if (s != null && !s.toString().isEmpty()) {
            try {
                val inilen: Int = et.text.length
                val v = s.toString()
                    .replace("$", "")
                val n = df.parse(v)
                val cp = et.selectionStart
                if (hasFractionalPart) {
                    val trailingZeros = StringBuilder()
                    while (trailingZeroCount-- > 0)
                        trailingZeros.append('0')
                    et.setText(df.format(n) + trailingZeros.toString())
                } else {
                    et.setText(dfnd.format(n))
                }
                et.setText(et.text.toString())
                val endlen: Int = et.text.length
                val sel = cp + (endlen - inilen)
                if (sel > 0 && sel < et.text.length) {
                    et.setSelection(sel)
                } else {
                    et.setSelection(et.text.length)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }

        et.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        var index = s.toString().indexOf(df.decimalFormatSymbols.decimalSeparator.toString())
        trailingZeroCount = 0
        if (index > -1) {
            index++
            while (index < s.length) {
                if (s[index] == '0')
                    trailingZeroCount++
                else {
                    trailingZeroCount = 0
                }
                index++
            }
            hasFractionalPart = true
        } else {
            hasFractionalPart = false
        }
    }
}