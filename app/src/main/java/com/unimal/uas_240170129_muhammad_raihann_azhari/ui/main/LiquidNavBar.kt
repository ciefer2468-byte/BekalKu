package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Navigasi bawah bergaya "liquid": gelembung oranye meregang saat berpindah tab lalu
 * menyatu kembali. Posisinya dikendalikan langsung oleh ViewPager2 lewat [setProgress]
 * sehingga ikut bergerak mulus saat halaman digeser.
 */
class LiquidNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onItemSelected: ((Int) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private var icons: List<Drawable> = emptyList()
    private var labels: List<String> = emptyList()
    private var progress = 0f

    private val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blobRect = RectF()
    private val evaluator = ArgbEvaluator()

    private val activeColor = Color.WHITE
    private val inactiveColor = ContextCompat.getColor(context, R.color.ink_soft)
    private val blobTop = ContextCompat.getColor(context, R.color.primary_light)
    private val blobBottom = ContextCompat.getColor(context, R.color.primary)

    fun setItems(@DrawableRes iconRes: IntArray, descriptions: Array<String>) {
        icons = iconRes.map { ContextCompat.getDrawable(context, it)!!.mutate() }
        labels = descriptions.toList()
        updateDescription()
        invalidate()
    }

    /** Posisi pecahan: 0f = tab pertama, 1.5f = di antara tab kedua dan ketiga, dst. */
    fun setProgress(value: Float) {
        val max = (icons.size - 1).coerceAtLeast(0).toFloat()
        progress = value.coerceIn(0f, max)
        updateDescription()
        invalidate()
    }

    private fun updateDescription() {
        labels.getOrNull(progress.roundToInt())?.let { contentDescription = it }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resolveSize((64 * density).toInt(), heightMeasureSpec)
        setMeasuredDimension(resolveSize(suggestedMinimumWidth, widthMeasureSpec), height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val d = BLOB_DIAMETER_DP * density
        blobPaint.shader = LinearGradient(
            0f, h / 2f - d / 2f, 0f, h / 2f + d / 2f, blobTop, blobBottom, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        val count = icons.size
        if (count == 0) return

        val itemWidth = width.toFloat() / count
        val centerY = height / 2f
        val diameter = BLOB_DIAMETER_DP * density

        // Efek cair: gelembung meregang di tengah perpindahan, lalu menyatu di tab tujuan.
        val frac = progress - floor(progress)
        val wave = sin(PI * frac).toFloat()
        val stretch = wave * STRETCH_DP * density
        val squash = wave * 5f * density
        val centerX = itemWidth * (progress + 0.5f)

        blobRect.set(
            centerX - diameter / 2f - stretch / 2f,
            centerY - diameter / 2f + squash / 2f,
            centerX + diameter / 2f + stretch / 2f,
            centerY + diameter / 2f - squash / 2f
        )
        canvas.drawRoundRect(blobRect, diameter / 2f, diameter / 2f, blobPaint)

        val half = ICON_SIZE_DP * density / 2f
        icons.forEachIndexed { index, icon ->
            val distance = abs(progress - index).coerceAtMost(1f)
            icon.setTint(evaluator.evaluate(distance, activeColor, inactiveColor) as Int)
            val cx = itemWidth * (index + 0.5f)
            icon.setBounds((cx - half).toInt(), (centerY - half).toInt(), (cx + half).toInt(), (centerY + half).toInt())
            val scale = 1f + 0.12f * (1f - distance)
            canvas.save()
            canvas.scale(scale, scale, cx, centerY)
            icon.draw(canvas)
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val count = icons.size
                if (count > 0) {
                    val index = (event.x / (width.toFloat() / count)).toInt().coerceIn(0, count - 1)
                    performClick()
                    onItemSelected?.invoke(index)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private companion object {
        const val BLOB_DIAMETER_DP = 46f
        const val ICON_SIZE_DP = 24f
        const val STRETCH_DP = 28f
    }
}
