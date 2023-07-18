package com.babilonia.presentation.view.circleview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.view.ViewCompat


abstract class ShapeOfView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val clipPaint = Paint(ANTI_ALIAS_FLAG)
    private val clipPath = Path()

    protected var pdMode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    var drawableImage: Drawable? = null
    private val clipManager = ClipPathManager()
    private var requiersShapeUpdate = true
    private var clipBitmap: Bitmap? = null

    internal val rectView = Path()


    override fun setBackground(background: Drawable) {
        //disabled here, please set a background to to this view child
        //super.setBackground(background);
    }

    override fun setBackgroundResource(resid: Int) {
        //disabled here, please set a background to to this view child
        //super.setBackgroundResource(resid);
    }

    override fun setBackgroundColor(color: Int) {
        //disabled here, please set a background to to this view child
        //super.setBackgroundColor(color);
    }

    init {
        clipPaint.isAntiAlias = true

        isDrawingCacheEnabled = true

        setWillNotDraw(false)

        clipPaint.color = Color.BLUE
        clipPaint.style = Paint.Style.FILL
        clipPaint.strokeWidth = 1F

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            clipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            setLayerType(View.LAYER_TYPE_SOFTWARE, clipPaint) //Only works for software layers
        } else {
            clipPaint.xfermode = pdMode
            setLayerType(View.LAYER_TYPE_HARDWARE, null) //Only works for software layers
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            requiresShapeUpdate()
        }
    }

    private fun requiresBitmap(): Boolean {
        return isInEditMode || clipManager.requiresBitmap() || drawableImage != null
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (requiersShapeUpdate) {
            calculateLayout(canvas.width, canvas.height)
            requiersShapeUpdate = false
        }
        if (requiresBitmap()) {
            clipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            clipBitmap?.let { canvas.drawBitmap(it, 0F, 0F, clipPaint) }
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                canvas.drawPath(clipPath, clipPaint)
            } else {
                canvas.drawPath(rectView, clipPaint)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    private fun calculateLayout(width: Int, height: Int) {
        rectView.reset()
        rectView.addRect(0f, 0f, 1f * getWidth(), 1f * getHeight(), Path.Direction.CW)

        if (width > 0 && height > 0) {
            clipManager.setupClipLayout(width, height)
            clipPath.reset()
            clipManager.createMask(width, height)?.let { clipPath.set(it) }

            if (requiresBitmap()) {
                if (clipBitmap != null) {
                    clipBitmap!!.recycle()
                }
                clipBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                clipBitmap?.let {
                    val canvas = Canvas(it)

                    if (drawableImage != null) {
                        drawableImage!!.setBounds(0, 0, width, height)
                        drawableImage!!.draw(canvas)
                    } else {
                        canvas.drawPath(clipPath, clipManager.getPaint())
                    }
                }
            }

            //invert the path for android P
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val success = rectView.op(clipPath, Path.Op.DIFFERENCE)
            }

            //this needs to be fixed for 25.4.0
            if (ViewCompat.getElevation(this) > 0f) {
                try {
                    outlineProvider = outlineProvider
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        postInvalidate()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getOutlineProvider(): ViewOutlineProvider {
        return object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val shadowConvexPath = clipManager.getShadowConvexPath()
                if (shadowConvexPath != null) {
                    try {
                        outline.setConvexPath(shadowConvexPath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            }
        }
    }

    fun setClipPathCreator(createClipPath: ClipPathManager.ClipPathCreator) {
        clipManager.setClipPathCreator(createClipPath)
        requiresShapeUpdate()
    }

    fun requiresShapeUpdate() {
        this.requiersShapeUpdate = true
        postInvalidate()
    }

}