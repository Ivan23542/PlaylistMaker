package com.example.playlistmaker.presentation.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.example.playlistmaker.R
import kotlin.math.min
import kotlin.math.roundToInt

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val iconRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null
    private var isPlaying = false
    private var isTouchTracking = false

    init {
        isClickable = true
        isFocusable = true

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            0
        )
        try {
            playBitmap = typedArray
                .getResourceId(R.styleable.PlaybackButtonView_playbackPlayImage, 0)
                .takeIf { it != 0 }
                ?.let(::createBitmap)
            pauseBitmap = typedArray
                .getResourceId(R.styleable.PlaybackButtonView_playbackPauseImage, 0)
                .takeIf { it != 0 }
                ?.let(::createBitmap)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        val contentWidth = (width - paddingLeft - paddingRight).coerceAtLeast(0)
        val contentHeight = (height - paddingTop - paddingBottom).coerceAtLeast(0)
        val bitmapWidth = maxOf(playBitmap?.width ?: 0, pauseBitmap?.width ?: 0)
        val bitmapHeight = maxOf(playBitmap?.height ?: 0, pauseBitmap?.height ?: 0)
        val iconWidth = min(bitmapWidth.takeIf { it > 0 } ?: contentWidth, contentWidth).toFloat()
        val iconHeight = min(bitmapHeight.takeIf { it > 0 } ?: contentHeight, contentHeight).toFloat()
        val left = paddingLeft + (contentWidth - iconWidth) / 2f
        val top = paddingTop + (contentHeight - iconHeight) / 2f

        iconRect.set(left, top, left + iconWidth, top + iconHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bitmap = if (isPlaying) pauseBitmap else playBitmap
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, iconRect, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return super.onTouchEvent(event)
        }

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchTracking = true
                isPressed = true
                true
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                if (isTouchTracking) {
                    isTouchTracking = false
                    switchPlaybackState()
                    performClick()
                }
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                isTouchTracking = false
                isPressed = false
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun setPlaying(isPlaying: Boolean) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying
            invalidate()
        }
    }

    fun switchPlaybackState() {
        setPlaying(!isPlaying)
    }

    private fun createBitmap(@DrawableRes drawableRes: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableRes)?.mutate() ?: return null
        val defaultIconSize = (DEFAULT_ICON_SIZE_DP * resources.displayMetrics.density).roundToInt()
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: defaultIconSize
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: defaultIconSize
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private companion object {
        private const val DEFAULT_ICON_SIZE_DP = 56
    }
}
