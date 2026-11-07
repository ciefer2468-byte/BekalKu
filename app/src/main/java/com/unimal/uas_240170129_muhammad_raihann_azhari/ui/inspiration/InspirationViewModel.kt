package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.inspiration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.remote.Inspiration
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.remote.MealApi
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import java.io.IOException

/** State: Error.message berisi "network" untuk gangguan koneksi, selain itu masalah server. */
class InspirationViewModel : ViewModel() {

    private val _state = MutableLiveData<UiState<Inspiration>>()
    val state: LiveData<UiState<Inspiration>> = _state

    fun load(force: Boolean = false) {
        if (!force && _state.value != null) return
        _state.value = UiState.Loading
        AppExecutors.io.execute {
            try {
                _state.postValue(UiState.Success(MealApi.fetchRandom()))
            } catch (e: IOException) {
                _state.postValue(UiState.Error(ERROR_NETWORK))
            } catch (e: Exception) {
                _state.postValue(UiState.Error(ERROR_SERVER))
            }
        }
    }

    companion object {
        const val ERROR_NETWORK = "network"
        const val ERROR_SERVER = "server"
    }
}
