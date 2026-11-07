package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.util.Calendar

/** Menambah padding atas sebesar tinggi status bar (untuk tampilan edge-to-edge). */
fun View.applyTopInsetPadding() {
    val initialTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        v.updatePadding(top = initialTop + top)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/** Indeks hari ini: 0 = Senin ... 6 = Minggu. */
fun todayIndex(): Int {
    val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Minggu = 1
    return (dow + 5) % 7
}
