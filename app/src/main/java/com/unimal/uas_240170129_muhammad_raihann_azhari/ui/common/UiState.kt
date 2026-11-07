package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common

/** State layar: loading, sukses, atau error. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String?) : UiState<Nothing>
}
