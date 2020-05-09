package com.danielecampogiani.customviews

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.res.use
import com.danielecampogiani.customviews.delegates.Invalidate.Companion.invalidateOnChange
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


data class Item(
    val name: String,
    val value: Float
)

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    companion object {
        private val DEFAULT_COLOR = ColorStateList.valueOf(Color.BLACK)
    }

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var items: List<Item> by emptyList<Item>().invalidateOnChange()
    var spacing: Float by 0f.invalidateOnChange()
    var itemColor: ColorStateList by DEFAULT_COLOR.invalidateOnChange()

    private var selected: Item? = null
    private var prevX = 0f

    private var animValue: Float = 1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        prevX = event.x
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {

        val currentItems = items

        if (currentItems.isEmpty()) {
            selected = null
            return super.performClick()
        }

        val viewportWidth = (width - paddingLeft - paddingRight).toFloat()
        val itemWidth = viewportWidth / currentItems.size

        val selectedIndex = max(
            0,
            min(
                floor((prevX - paddingLeft) / itemWidth).toInt(),
                currentItems.size - 1
            )
        )
        selected = currentItems[selectedIndex]
        invalidate()
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentItems = if (!isInEditMode) items else fakeData
        if (currentItems.isEmpty()) return

        paint.color = itemColor.defaultColor

        val viewPortWidth: Float = (width - paddingLeft - paddingRight).toFloat()
        val viewPortHeight: Float = (height - paddingTop - paddingBottom).toFloat()
        val itemWidth = viewPortWidth / currentItems.size

        val maxItemHeight = currentItems.maxBy { it.value }!!.value

        currentItems.forEachIndexed { index, item ->
            paint.color = itemColor.getColorForState(
                getDrawableStateSelected(item == selected),
                itemColor.defaultColor
            )

            canvas.drawRect(
                paddingLeft + index * itemWidth + spacing / 2,
                height - paddingBottom - viewPortHeight / maxItemHeight * (item.value * animValue),
                paddingLeft + (index + 1) * itemWidth - spacing / 2,
                height - paddingBottom.toFloat(),
                paint
            )
        }
    }

    private fun getDrawableStateSelected(selected: Boolean): IntArray {
        return if (!selected) {
            drawableState
        } else {
            val newState: IntArray = Arrays.copyOf(drawableState, drawableState.size + 1)
            newState[newState.size - 1] = android.R.attr.state_selected
            newState
        }
    }

    private fun buildDefaultColorStateList(): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf())
        val colors = intArrayOf(
            context.resolveColorAttr(R.attr.colorSecondary),
            context.resolveColorAttr(R.attr.colorOnSurface)
        )
        return ColorStateList(states, colors)
    }

    fun animateChart(
        interpolator: TimeInterpolator = OvershootInterpolator(),
        duration: Long = context.resources.getInteger(android.R.integer.config_longAnimTime)
            .toLong()
    ) {
        animValue = 0f
        ObjectAnimator.ofFloat(0f, 1f).apply {
            this.interpolator = interpolator
            this.duration = duration
            addUpdateListener {
                animValue = it.animatedValue!! as Float
                invalidate()
            }
        }.start()
    }

    private val fakeData = listOf(
        Item("1", 1f),
        Item("2", 2f),
        Item("3", 1.5f),
        Item("4", 0.5f)
    )

    init {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.ChartView).use {
                itemColor = it.getColorStateList(R.styleable.ChartView_dc_item_color)
                    ?: buildDefaultColorStateList()
                spacing = it.getDimension(R.styleable.ChartView_dc_item_spacing, 0f)
            }
        }
        isClickable = true
    }
}