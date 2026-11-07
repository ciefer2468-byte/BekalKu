package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.util.Patterns

object Validators {
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
