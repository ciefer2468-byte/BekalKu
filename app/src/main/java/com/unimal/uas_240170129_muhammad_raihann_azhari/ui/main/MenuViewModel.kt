package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.MenuRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.todayIndex

/** ViewModel bersama untuk kelima tab (Beranda, Menu, Rencana, Statistik, Profil). */
class MenuViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MenuRepository(app)
    private val session = SessionManager(app)

    private val _state = MutableLiveData<UiState<List<BekalMenu>>>(UiState.Loading)
    val state: LiveData<UiState<List<BekalMenu>>> = _state

    // State filter/pencarian bertahan saat layar diputar.
    var searchQuery: String = ""
    var menuCategory: Int = ALL
    var homeCategory: Int = ALL
    var plannerDay: Int = todayIndex()

    /** Muat ulang data. Panggil dari main thread. */
    fun load(silent: Boolean = false) {
        if (!silent || _state.value !is UiState.Success) _state.value = UiState.Loading
        val userId = session.userId
        AppExecutors.io.execute {
            try {
                _state.postValue(UiState.Success(repo.getAll(userId)))
            } catch (e: Exception) {
                _state.postValue(UiState.Error(e.message))
            }
        }
    }

    /** Tandai bekal sudah/belum dibawa (optimistic update). */
    fun togglePacked(menu: BekalMenu) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        val newValue = !menu.packed
        _state.value = UiState.Success(current.map { if (it.id == menu.id) it.copy(packed = newValue) else it })
        val userId = session.userId
        AppExecutors.io.execute {
            try {
                repo.setPacked(menu.id, userId, newValue)
            } catch (e: Exception) {
                _state.postValue(UiState.Error(e.message))
            }
        }
    }

    companion object {
        const val ALL = -1
    }
}

/** Filter berdasarkan kata kunci (nama / bahan) dan kategori. */
fun List<BekalMenu>.filterBy(query: String, category: Int): List<BekalMenu> {
    val q = query.trim().lowercase()
    return filter { menu ->
        (category == MenuViewModel.ALL || menu.category == category) &&
            (q.isEmpty() || menu.name.lowercase().contains(q) || menu.ingredients.lowercase().contains(q))
    }
}
