package com.example.customclockview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.customclockview.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        with(binding) {
            if (savedInstanceState != null) {
                clockView.restoreInstanceState(savedInstanceState.getBundle("clockViewState")!!)
                clockViewSmall.restoreInstanceState(savedInstanceState.getBundle("smallClockViewState")!!)
            }

            clockView.setTime(hour, minute, second)
            clockView.startUpdatingTime()

            clockViewSmall.setTime(hour, minute, second)
            clockViewSmall.startUpdatingTime()

            colorButton.setOnClickListener {
                clockView.setRandomClockColor()
                clockViewSmall.setRandomClockColor()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("clockViewState", binding.clockView.saveInstanceState())
        outState.putParcelable("smallClockViewState", binding.clockViewSmall.saveInstanceState())
    }
}
