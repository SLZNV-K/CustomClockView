package com.example.customclockview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.View
import com.example.customclockview.R
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0
    private var radius: Float = 0f

    private var numberTextSize: Float = 60F
    private var numberFontFamily: String = "sans-serif"
    private var backgroundCircleColor: Int

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ClockView).apply {
            numberTextSize = getDimension(
                R.styleable.ClockView_numberTextSize,
                numberTextSize
            )
            numberFontFamily =
                getString(R.styleable.ClockView_numberFontFamily) ?: numberFontFamily
            backgroundCircleColor =
                getColor(R.styleable.ClockView_backgroundCircleColor, Color.WHITE)
            recycle()
        }
    }

    private val clockAroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        setShadowLayer(55f, 30f, 30f, Color.GRAY)
        isAntiAlias = true
    }

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        setShadowLayer(10f, 15f, 15f, Color.GRAY)
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = backgroundCircleColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val numberPaint = Paint().apply {
        textSize = numberTextSize
        val fontFamily = Typeface.create(numberFontFamily, Typeface.BOLD)
        typeface = fontFamily
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
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
        canvas.drawCircle(centerX, centerY, radius, clockAroundPaint)

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
        val radius = (width.coerceAtMost(height) / 2 * 0.6).toFloat()
        for (i in 1..12) {
            val angle = Math.PI / 6 * (i - 3)
            val x = (centerX + cos(angle) * radius).toFloat()
            val y = (centerY + sin(angle) * radius).toFloat()
            canvas.drawText(i.toString(), x, y + 20, numberPaint)
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