package com.example.customclockview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.View
import com.example.customclockview.R
import com.example.customclockview.utills.AndroidUtils
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0
    private var radius: Float = 0f
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        numberPaint.textSize = min(w, h) / 10f
    }

    private var numberTextSize: Float = AndroidUtils.sp(context, 30f).toFloat()
    private var numberFont: String = "sans-serif"
    private var backgroundCircleColor: Int

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ClockView).apply {
            numberTextSize = getDimension(
                R.styleable.ClockView_numberTextSize,
                numberTextSize
            )
            numberFont =
                getString(R.styleable.ClockView_numberFontFamily) ?: numberFont
            backgroundCircleColor =
                getColor(R.styleable.ClockView_backgroundCircleColor, Color.WHITE)
            recycle()
        }
    }

    private val clockAroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint().apply {
        color = backgroundCircleColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val numberPaint = Paint().apply {
        val fontFamily = Typeface.create(numberFont, Typeface.BOLD)
        typeface = fontFamily
        textSize = numberTextSize
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    fun setRandomClockColor() {
        backgroundPaint.color = getRandomColor()
    }

    fun saveInstanceState(): Bundle {
        val bundle = Bundle()
        bundle.putInt("backgroundColor", backgroundPaint.color)
        return bundle
    }

    fun restoreInstanceState(state: Bundle) {
        backgroundPaint.color = state.getInt("backgroundColor")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX: Float = width / 2f
        val centerY: Float = height / 2f

        radius = min(centerX, centerY) * 0.8f

        drawClock(canvas, centerX, centerY)
        drawHourHand(canvas)
        drawMinuteHand(canvas)
        drawSecondHand(canvas)
        drawNumbers(canvas, centerX, centerY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth: Int = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight: Int = suggestedMinimumHeight + paddingTop + paddingBottom
        val width: Int = measureDimension(desiredWidth, widthMeasureSpec)
        val height: Int = measureDimension(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun drawClock(canvas: Canvas, centerX: Float, centerY: Float) {
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        clockAroundPaint.strokeWidth = radius * 0.1f
        clockAroundPaint.setShadowLayer(
            radius * 0.1f,
            radius * 0.07f,
            radius * 0.07f,
            darkenColor(clockAroundPaint.color)
        )
        canvas.drawCircle(centerX, centerY, radius, clockAroundPaint)

        drawDots(centerX, centerY, canvas)
    }

    private fun drawDots(
        centerX: Float,
        centerY: Float,
        canvas: Canvas
    ) {
        paint.style = Paint.Style.FILL
        val dotRadius = radius * 0.01f
        for (i in 0 until 60) {
            val angle = Math.PI / 30 * i
            val x = (centerX + cos(angle) * radius * 0.9f).toFloat()
            val y = (centerY + sin(angle) * radius * 0.9f).toFloat()
            canvas.drawCircle(x, y, dotRadius, paint)
        }
    }

    private fun drawHand(canvas: Canvas, angle: Double, length: Float) {
        val centerX: Float = width / 2f
        val centerY: Float = height / 2f
        val x = centerX + length * sin(angle).toFloat()
        val y = centerY - length * cos(angle).toFloat()
        paint.setShadowLayer(
            radius * 0.02f,
            radius * 0.03f,
            radius * 0.03f,
            darkenColor(clockAroundPaint.color)
        )

        canvas.drawLine(centerX, centerY, x, y, paint)
    }

    private fun drawHourHand(canvas: Canvas) {
        val hourAngle = Math.toRadians((hour) * 30 + (0.5 * minute))
        val hourAngleEnd = Math.toRadians((hour) * 30 + (0.5 * minute) - 180)
        paint.strokeWidth = radius * 0.03f
        drawHand(canvas, hourAngle, radius * 0.5f)
        drawHand(canvas, hourAngleEnd, radius * 0.1f)
    }

    private fun drawMinuteHand(canvas: Canvas) {
        val minuteAngle = Math.toRadians((minute) * 6 + (0.1 * second))
        val minuteAngleEnd = Math.toRadians((minute) * 6 + (0.1 * second) - 180)
        paint.strokeWidth = radius * 0.02f
        drawHand(canvas, minuteAngle, radius * 0.7f)
        drawHand(canvas, minuteAngleEnd, radius * 0.2f)
    }

    private fun drawSecondHand(canvas: Canvas) {
        val secondAngle = Math.toRadians(((second) * 6).toDouble())
        val secondAngleEnd = Math.toRadians(((second) * 6).toDouble() - 180)
        paint.strokeWidth = radius * 0.01f
        drawHand(canvas, secondAngle, radius * 0.9f)
        drawHand(canvas, secondAngleEnd, radius * 0.3f)
    }

    private fun drawNumbers(canvas: Canvas, centerX: Float, centerY: Float) {
        val radius = (min(width, height) / 2 * 0.6).toFloat()
        for (i in 1..12) {
            val angle = Math.PI / 6 * (i - 3)
            val x = (centerX + cos(angle) * radius).toFloat()
            val y = (centerY + sin(angle) * radius).toFloat()
            canvas.drawText(i.toString(), x, y + (numberPaint.textSize / 3f), numberPaint)
        }
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        val specMode: Int = MeasureSpec.getMode(measureSpec)
        val specSize: Int = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(specSize)
            else -> desiredSize
        }
    }

    private fun darkenColor(color: Int): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * 0.8).roundToInt()
        val g = (Color.green(color) * 0.8).roundToInt()
        val b = (Color.blue(color) * 0.8).roundToInt()
        return Color.argb(a, min(r, 255), min(g, 255), min(b, 255))
    }

    private fun getRandomColor(): Int {
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)
        return Color.argb(255, red, green, blue)
    }

    fun setTime(hour: Int, minute: Int, second: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
        invalidate()
    }

    fun startUpdatingTime() {
        val handlerThread = HandlerThread("TimeUpdateThread")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        handler.post(object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                setTime(
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)
                )
                handler.postDelayed(this, 1000)
            }
        })
    }
}
