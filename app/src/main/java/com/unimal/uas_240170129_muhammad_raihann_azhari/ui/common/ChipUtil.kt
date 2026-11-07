package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common

import com.unimal.uas_240170129_muhammad_raihann_azhari.R

/** Pemetaan antara id Chip kategori dan indeks kategori (-1 = semua). */
object ChipUtil {
    fun categoryOf(chipId: Int): Int = when (chipId) {
        R.id.chipCat0 -> 0
        R.id.chipCat1 -> 1
        R.id.chipCat2 -> 2
        R.id.chipCat3 -> 3
        else -> -1
    }

    fun chipIdOf(category: Int): Int = when (category) {
        0 -> R.id.chipCat0
        1 -> R.id.chipCat1
        2 -> R.id.chipCat2
        3 -> R.id.chipCat3
        else -> R.id.chipAll
    }
}
