package com.example.customclockview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.customclockview.ui.ClockView
import java.util.Calendar

class MainActivity : AppCompatActivity() {


    private lateinit var clockView: ClockView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockView = findViewById(R.id.clockView)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        clockView.setTime(hour, minute, second)

        clockView.startUpdatingTime()
    }
}
