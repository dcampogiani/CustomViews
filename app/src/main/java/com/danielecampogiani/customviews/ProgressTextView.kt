package com.danielecampogiani.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use
import com.danielecampogiani.customviews.delegates.Invalidate.Companion.invalidateOnChange
import com.danielecampogiani.customviews.delegates.RequestLayout.Companion.requestLayoutOnChange
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


class ProgressTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    companion object {
        private val DEFAULT_COLOR = ColorStateList.valueOf(Color.WHITE)
        private val DEFAULT_TEXT_COLOR = ColorStateList.valueOf(Color.BLACK)
        private val DEFAULT_PROGRESS_TEXT_COLOR = ColorStateList.valueOf(Color.WHITE)
        private const val DEFAULT_TEXT_SIZE = 80.0f
    }

    var progress: Float by 0f.invalidateOnChange()
    var progressColor: ColorStateList by DEFAULT_COLOR.invalidateOnChange()
    var progressTextColor: ColorStateList by DEFAULT_PROGRESS_TEXT_COLOR.invalidateOnChange()
    var textColor: ColorStateList by DEFAULT_TEXT_COLOR.invalidateOnChange()
    var text: String by "".requestLayoutOnChange()

    private var paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private var layout: StaticLayout? = null


    var textSize: Float
        get() = paint.textSize
        set(value) {
            paint.textSize = value
            requestLayout()
        }

    init {
        if (attrs != null) {

            context.obtainStyledAttributes(attrs, R.styleable.ProgressTextView).use {
                progress = it.getFloat(R.styleable.ProgressTextView_dc_progress, 0.0f)
                progressColor =
                    it.getColorStateList(R.styleable.ProgressTextView_dc_progressColor)
                        ?: context.resolveColorAttr(R.attr.colorOnSurface).asColorStateList()
                progressTextColor =
                    it.getColorStateList(R.styleable.ProgressTextView_dc_progressTextColor)
                        ?: context.resolveColorAttr(R.attr.colorSurface).asColorStateList()
                text = it.getString(R.styleable.ProgressTextView_dc_text).orEmpty()
                textColor = it.getColorStateList(R.styleable.ProgressTextView_dc_text_color)
                    ?: context.resolveColorAttr(R.attr.colorOnSurface).asColorStateList()
                textSize =
                    it.getDimension(R.styleable.ProgressTextView_dc_text_size, DEFAULT_TEXT_SIZE)
            }

            if (isInEditMode) {
                progress = Random.Default.nextFloat()
                text = "Progress Text"
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val withMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val withSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width = paddingLeft + paddingRight
        var height = paddingTop + paddingBottom

        if (withMode == MeasureSpec.EXACTLY) {
            width = withSize
        } else {
            val textLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                paint,
                Integer.MAX_VALUE
            ).build().also { layout = it }

            width += (0 until textLayout.lineCount)
                .map { textLayout.getLineWidth(it) }
                .max()?.toInt() ?: 0

            width = max(width, suggestedMinimumWidth)
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            val textLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                paint,
                width
            ).build().also { layout = it }

            height += textLayout.height
            height = max(height, suggestedMinimumHeight)
            if (heightMode == MeasureSpec.AT_MOST) {
                height = min(height, heightSize)
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            width
        ).build()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var saveCount = canvas.save()
        canvas.clipRect(0.0f, 0.0f, progress * width, height.toFloat())

        paint.color = progressColor.getColorForState(drawableState, progressColor.defaultColor)
        canvas.drawRect(0f, 0f, progress * width, height.toFloat(), paint)

        layout?.let {
            canvas.translate(
                paddingLeft.toFloat(),
                max(paddingTop.toFloat(), (height - it.height) / 2.0f)
            )
            paint.color =
                progressTextColor.getColorForState(drawableState, progressTextColor.defaultColor)
            it.draw(canvas)
        }

        canvas.restoreToCount(saveCount)

        saveCount = canvas.save()
        canvas.clipRect(progress * width, 0.0f, width.toFloat(), height.toFloat())

        layout?.let {
            canvas.translate(
                paddingLeft.toFloat(),
                max(paddingTop.toFloat(), (height - it.height) / 2.0f)
            )
            paint.color = textColor.getColorForState(drawableState, textColor.defaultColor)
            it.draw(canvas)
        }
        canvas.restoreToCount(saveCount)
    }


}