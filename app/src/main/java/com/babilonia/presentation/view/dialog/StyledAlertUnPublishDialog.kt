package com.babilonia.presentation.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.presentation.extension.invisible
import com.google.android.material.textfield.TextInputEditText

class StyledAlertUnPublishDialog private constructor(
    context: Context,
    private val onRadioButtonSelected: (String) -> Unit
) : Dialog(context) {

    private var onRadioButtonSelectedListener: ((String) -> Unit)? = null
    private lateinit var title: TextView
    private lateinit var body: TextView
    private lateinit var rbBabilonia: RadioButton
    private lateinit var rbPortal: RadioButton
    private lateinit var rbSocial: RadioButton
    private lateinit var rbReferrals: RadioButton
    private lateinit var rbSell: RadioButton
    private lateinit var leftButton: TextView
    private lateinit var rightButton: TextView
    private var infoReason: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_alert_un_publish)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initViews()
    }

    private fun setTitleText(text: String) {
        if (text.isEmpty()) {
            title.invisible()
        } else {
            title.text = text
        }
    }

    fun setBodyTextBabilonia(text: String) {
        if (text.isEmpty()) {
            rbBabilonia.invisible()
        } else {
            rbBabilonia.text = text
        }
    }

    fun setBodyTextPortal(text: String) {
        if (text.isEmpty()) {
            rbPortal.invisible()
        } else {
            rbPortal.text = text
        }
    }

    fun setBodyTextSocial(text: String) {
        if (text.isEmpty()) {
            rbSocial.invisible()
        } else {
            rbSocial.text = text
        }
    }

    fun setBodyTextReferrals(text: String) {
        if (text.isEmpty()) {
            rbReferrals.invisible()
        } else {
            rbReferrals.text = text
        }
    }

    fun setBodyTextSell(text: String) {
        if (text.isEmpty()) {
            rbSell.invisible()
        } else {
            rbSell.text = text
        }    }

    fun setBodyReasonSell(text: String) {
        val etReason = findViewById<TextInputEditText>(R.id.etReason)
        etReason?.let {
            it.text = Editable.Factory.getInstance().newEditable(text)
            infoReason = it.text.toString()
        }
    }

    private fun setBodyText(text: String) {
        if (text.isEmpty()) {
            body.invisible()
        } else {
            body.text = text
        }
    }

    private fun setRightButton(
        text: String,
        @ColorRes textColor: Int = R.color.colorPrimary,
        onClick: ((String) -> Unit)? = null
    ) {
        rightButton.text = text
        rightButton.setTextColor(ContextCompat.getColor(context, textColor))
        rightButton.setOnClickListener {
            onClick?.invoke(infoReason)
            dismiss()
        }
    }

    private fun setLeftButton(
        text: String,
        @ColorRes textColor: Int = R.color.black,
        onClick: (() -> Unit)? = null) {
        leftButton.text = text
        leftButton.setTextColor(ContextCompat.getColor(context, textColor))
        leftButton.setOnClickListener {
            onClick?.invoke()
            dismiss()
        }
    }

    fun setOnRadioButtonSelectedListener(listener: (String) -> Unit): StyledAlertUnPublishDialog {
        onRadioButtonSelectedListener = listener
        return this
    }

    private fun setOnShowAction(onShow: (StyledAlertUnPublishDialog) -> Unit) {
        setOnShowListener {
            onShow(this)
        }
    }

    private fun initViews() {
        title = findViewById(R.id.tvTitle)
        body = findViewById(R.id.tvBody)
        rbBabilonia = findViewById(R.id.rbBabilonia)
        rbPortal = findViewById(R.id.rbPortal)
        rbSocial = findViewById(R.id.rbSocial)
        rbReferrals = findViewById(R.id.rbReferrals)
        rbSell = findViewById(R.id.rbSell)
        rightButton = findViewById(R.id.btnPositive)
        leftButton = findViewById(R.id.btnNegative)

        rbBabilonia.isChecked = true
        onRadioButtonSelected("babilonia")
        val radioButtons = listOf(rbBabilonia, rbPortal, rbSocial, rbReferrals, rbSell)
        for (radioButton in radioButtons) {
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onRadioButtonSelected(radioButton.text.toString())
                }
            }
        }
        for (radioButton in radioButtons) {
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val selectedValue = when (radioButton) {
                        rbBabilonia -> "babilonia"
                        rbPortal -> "other"
                        rbSocial -> "rrss"
                        rbReferrals -> "referal"
                        rbSell -> "unsold"
                        else -> null
                    }
                    selectedValue?.let { onRadioButtonSelected(it) }
                }
            }
        }
    }
    
    class Builder(private var context: Context) {
        private var titleText: String = EmptyConstants.EMPTY_STRING
        private var bodyText: String = EmptyConstants.EMPTY_STRING
        private var rbTextBabilonia: String = EmptyConstants.EMPTY_STRING
        private var rbTextPortal: String = EmptyConstants.EMPTY_STRING
        private var rbTextSocial: String = EmptyConstants.EMPTY_STRING
        private var rbTextReferrals: String = EmptyConstants.EMPTY_STRING
        private var rbTextSell: String = EmptyConstants.EMPTY_STRING
        private var rightButtonText: String = EmptyConstants.EMPTY_STRING
        private var leftButtonText: String = EmptyConstants.EMPTY_STRING
        private var reason: String = EmptyConstants.EMPTY_STRING
        private var rightButtonColor = R.color.colorAccent
        private var leftButtonColor = R.color.black
        private var onRightClickCallback: ((String) -> Unit)? = null
        private var onLeftClickCallback: (() -> Unit)? = null
        private var isCustomDialogCancelable: Boolean = true
        private var onDismissListener: (() -> Unit)? = null
        private var onRadioButtonSelected: (String) -> Unit = {}

        fun setOnRadioButtonSelectedListener(listener: (String) -> Unit): Builder {
            onRadioButtonSelected = listener
            return this
        }

        fun setTitleText(text: String): Builder {
            titleText = text
            return this
        }

        fun setBodyText(text: String): Builder {
            bodyText = text
            return this
        }

        fun setBodyTextBabilonia(text: String): Builder {
            rbTextBabilonia = text
            return this
        }

        fun setBodyTextPortal(text: String): Builder {
            rbTextPortal = text
            return this
        }

        fun setBodyTextSocial(text: String): Builder {
            rbTextSocial = text
            return this
        }

        fun setBodyTextReferrals(text: String): Builder {
            rbTextReferrals = text
            return this
        }

        fun setBodyReasonSell(text: String): Builder {
            reason = text
            return this
        }

        fun setBodyTextSell(text: String): Builder {
            rbTextSell = text
            return this
        }

        fun setRightButton(
            text: String,
            @ColorRes textColor: Int = R.color.colorPrimary,
            onClick: ((String) -> Unit)? = null
        ): Builder {
            rightButtonText = text
            rightButtonColor = textColor
            onRightClickCallback = onClick
            return this
        }

        fun setLeftButton(
            text: String,
            @ColorRes textColor: Int = R.color.black,
            onClick: (() -> Unit)? = null
        ): Builder {
            leftButtonText = text
            leftButtonColor = textColor
            onLeftClickCallback = onClick
            return this
        }

        fun setIsCancellable(isCancellable: Boolean): Builder {
            isCustomDialogCancelable = isCancellable
            return this
        }

        fun setOnDismissListener(onDismiss: () -> Unit): Builder {
            onDismissListener = onDismiss
            return this
        }

        fun build(): StyledAlertUnPublishDialog {
            return StyledAlertUnPublishDialog(context, onRadioButtonSelected).apply {
                setOnShowAction {
                    it.setTitleText(titleText)
                    it.setBodyText(bodyText)
                    it.setBodyTextBabilonia(rbTextBabilonia)
                    it.setBodyTextPortal(rbTextPortal)
                    it.setBodyTextSocial(rbTextSocial)
                    it.setBodyTextReferrals(rbTextReferrals)
                    it.setBodyTextSell(rbTextSell)
                    it.setBodyReasonSell(reason)
                    it.setRightButton(rightButtonText, rightButtonColor) { infoReason ->
                        onRightClickCallback?.invoke(infoReason)
                    }
                    it.setLeftButton(leftButtonText, leftButtonColor, onLeftClickCallback)
                    it.setCancelable(isCustomDialogCancelable)
                    it.setOnRadioButtonSelectedListener(onRadioButtonSelected)
                    onDismissListener?.let { onDismiss ->
                        it.setOnDismissListener { onDismiss() }
                    }
                }
            }
        }
    }
}