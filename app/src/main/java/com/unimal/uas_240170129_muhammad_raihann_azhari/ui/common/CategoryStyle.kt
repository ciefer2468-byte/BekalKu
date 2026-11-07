package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common

import androidx.annotation.DrawableRes
import com.unimal.uas_240170129_muhammad_raihann_azhari.R

object CategoryStyle {
    @DrawableRes
    fun gradient(category: Int): Int = when (category) {
        0 -> R.drawable.bg_card_sarapan
        1 -> R.drawable.bg_card_siang
        2 -> R.drawable.bg_card_malam
        else -> R.drawable.bg_card_camilan
    }
}
