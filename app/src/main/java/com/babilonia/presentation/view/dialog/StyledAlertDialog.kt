package com.babilonia.presentation.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.presentation.extension.invisible

class StyledAlertDialog private constructor(context: Context) : Dialog(context) {

    private lateinit var title: TextView
    private lateinit var body: TextView
    private lateinit var leftButton: TextView
    private lateinit var rightButton: TextView
    private lateinit var dividerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_alert)
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
        onClick: (() -> Unit)? = null
    ) {
        rightButton.text = text
        rightButton.setTextColor(ContextCompat.getColor(context, textColor))
        rightButton.setOnClickListener {
            onClick?.invoke()
            dismiss()
        }
    }

    private fun setLeftButton(
        text: String,
        @ColorRes textColor: Int = R.color.black,
        onClick: (() -> Unit)? = null
    ) {
        if (text.isEmpty()) {
            leftButton.visibility = View.GONE
            dividerView.visibility = View.GONE
        } else {
            leftButton.text = text
            leftButton.setTextColor(ContextCompat.getColor(context, textColor))
            leftButton.setOnClickListener {
                onClick?.invoke()
                dismiss()
            }
        }
    }

    private fun setOnShowAction(onShow: (StyledAlertDialog) -> Unit) {
        setOnShowListener {
            onShow(this)
        }
    }

    private fun initViews() {
        title = findViewById(R.id.tvTitle)
        body = findViewById(R.id.tvBody)
        rightButton = findViewById(R.id.btnPositive)
        leftButton = findViewById(R.id.btnNegative)
        dividerView = findViewById(R.id.divider)
    }

    class Builder(private var context: Context) {
        private var titleText: String = EmptyConstants.EMPTY_STRING
        private var bodyText: String = EmptyConstants.EMPTY_STRING
        private var rightButtonText: String = EmptyConstants.EMPTY_STRING
        private var leftButtonText: String = EmptyConstants.EMPTY_STRING
        private var rightButtonColor = R.color.colorAccent
        private var leftButtonColor = R.color.black
        private var onRightClickCallback: (() -> Unit)? = null
        private var onLeftClickCallback: (() -> Unit)? = null
        private var isCustomDialogCancelable: Boolean = true
        private var onDismissListener: (() -> Unit)? = null

        fun setTitleText(text: String): StyledAlertDialog.Builder {
            titleText = text
            return this
        }

        fun setBodyText(text: String): StyledAlertDialog.Builder {
            bodyText = text
            return this
        }

        fun setRightButton(
            text: String,
            @ColorRes textColor: Int = R.color.colorPrimary,
            onClick: (() -> Unit)? = null
        ): StyledAlertDialog.Builder {
            rightButtonText = text
            rightButtonColor = textColor
            onRightClickCallback = onClick
            return this
        }

        fun setLeftButton(
            text: String,
            @ColorRes textColor: Int = R.color.black,
            onClick: (() -> Unit)? = null
        ): StyledAlertDialog.Builder {
            leftButtonText = text
            leftButtonColor = textColor
            onLeftClickCallback = onClick
            return this
        }

        fun setIsCancellable(isCancellable: Boolean): StyledAlertDialog.Builder {
            isCustomDialogCancelable = isCancellable
            return this
        }

        fun setOnDismissListener(onDismiss: () -> Unit): StyledAlertDialog.Builder {
            onDismissListener = onDismiss
            return this
        }

        fun build(): StyledAlertDialog {
            return StyledAlertDialog(context).apply {
                setOnShowAction {
                    it.setTitleText(titleText)
                    it.setBodyText(bodyText)
                    it.setRightButton(rightButtonText, rightButtonColor, onRightClickCallback)
                    it.setLeftButton(leftButtonText, leftButtonColor, onLeftClickCallback)
                    it.setCancelable(isCustomDialogCancelable)
                    onDismissListener?.let { onDismiss ->
                        it.setOnDismissListener { onDismiss() }
                    }
                }
            }
        }
    }
}