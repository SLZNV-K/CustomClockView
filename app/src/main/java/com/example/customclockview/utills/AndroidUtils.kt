package com.example.customclockview.utills

import android.content.Context
import android.util.TypedValue

object AndroidUtils {
    fun sp(context: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.resources.displayMetrics)
            .toInt()
}