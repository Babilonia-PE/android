package com.babilonia.presentation.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.babilonia.EmptyConstants.EMPTY_STRING
import com.babilonia.R

class SingleChoiceAlertDialog private constructor(context: Context) : Dialog(context) {

    private lateinit var title: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var leftButton: TextView
    private lateinit var rightButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_alert_single_choice)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initViews()
    }

    private fun setTitleText(text: String) {
        title.text = text
    }

    private fun setSingleChoiceItems(items: Array<String>, pickedItemIndex: Int, onItemPicked: (Int) -> Unit) {
        for (i in items.indices) {
            val item = layoutInflater.inflate(R.layout.layout_radiobutton, radioGroup, false) as RadioButton
            item.id = (i)
            item.text = items[i]
            radioGroup.addView(item)
        }
        radioGroup.check(pickedItemIndex)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            onItemPicked(checkedId)
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
        onClick: (() -> Unit)? = null) {
        leftButton.text = text
        leftButton.setTextColor(ContextCompat.getColor(context, textColor))
        leftButton.setOnClickListener {
            onClick?.invoke()
            dismiss()
        }
    }

    private fun setOnShowAction(onShow: (SingleChoiceAlertDialog) -> Unit) {
        setOnShowListener {
            onShow(this)
        }
    }

    private fun initViews() {
        title = findViewById(R.id.tvTitle)
        radioGroup = findViewById(R.id.rgItems)
        rightButton = findViewById(R.id.btnPositive)
        leftButton = findViewById(R.id.btnNegative)
    }

    class Builder(private var context: Context) {
        private var titleText: String = EMPTY_STRING
        private var rightButtonText: String = EMPTY_STRING
        private var leftButtonText: String = EMPTY_STRING
        private var rightButtonColor = R.color.colorAccent
        private var leftButtonColor = R.color.black
        private var onRightClickCallback: (() -> Unit)? = null
        private var onLeftClickCallback: (() -> Unit)? = null
        private var isCustomDialogCancelable: Boolean = true
        private var onDismissListener: (() -> Unit)? = null
        private var singleChoiceItems: Array<String> = emptyArray()
        private var prepickedItemIndex = 0
        private var onItemPickedCallback: (Int) -> Unit = {}

        fun setTitleText(text: String): SingleChoiceAlertDialog.Builder {
            titleText = text
            return this
        }

        fun setSingleChoiceItems(
            items: Array<String>,
            pickedItemIndex: Int,
            onItemPicked: (Int) -> Unit
        ): SingleChoiceAlertDialog.Builder {
            singleChoiceItems = items
            prepickedItemIndex = pickedItemIndex
            onItemPickedCallback = onItemPicked
            return this
        }

        fun setRightButton(
            text: String,
            @ColorRes textColor: Int = R.color.colorPrimary,
            onClick: (() -> Unit)? = null
        ): SingleChoiceAlertDialog.Builder {
            rightButtonText = text
            rightButtonColor = textColor
            onRightClickCallback = onClick
            return this
        }

        fun setLeftButton(
            text: String,
            @ColorRes textColor: Int = R.color.black,
            onClick: (() -> Unit)? = null
        ): SingleChoiceAlertDialog.Builder {
            leftButtonText = text
            leftButtonColor = textColor
            onLeftClickCallback = onClick
            return this
        }

        fun setIsCancellable(isCancellable: Boolean): SingleChoiceAlertDialog.Builder {
            isCustomDialogCancelable = isCancellable
            return this
        }

        fun setOnDismissListener(onDismiss: () -> Unit): SingleChoiceAlertDialog.Builder {
            onDismissListener = onDismiss
            return this
        }

        fun build(): SingleChoiceAlertDialog {
            return SingleChoiceAlertDialog(context).apply {
                setOnShowAction {
                    it.setTitleText(titleText)
                    it.setSingleChoiceItems(singleChoiceItems, prepickedItemIndex, onItemPickedCallback)
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