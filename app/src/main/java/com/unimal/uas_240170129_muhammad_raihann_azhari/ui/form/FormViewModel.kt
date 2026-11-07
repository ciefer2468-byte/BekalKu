package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.MenuRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager

enum class SaveState { IDLE, SAVING, SAVED, FAILED }

class FormViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MenuRepository(app)
    private val session = SessionManager(app)

    private val _existing = MutableLiveData<BekalMenu?>()
    val existing: LiveData<BekalMenu?> = _existing

    private val _saveState = MutableLiveData(SaveState.IDLE)
    val saveState: LiveData<SaveState> = _saveState

    val userId: Long get() = session.userId

    fun loadForEdit(id: Long) {
        if (_existing.value != null) return
        val uid = session.userId
        AppExecutors.io.execute {
            val menu = try {
                repo.getById(id, uid)
            } catch (e: Exception) {
                null
            }
            _existing.postValue(menu)
        }
    }

    fun save(menu: BekalMenu) {
        _saveState.value = SaveState.SAVING
        AppExecutors.io.execute {
            try {
                if (menu.id > 0) repo.update(menu) else repo.insert(menu)
                _saveState.postValue(SaveState.SAVED)
            } catch (e: Exception) {
                _saveState.postValue(SaveState.FAILED)
            }
        }
    }

    fun consumeFailure() {
        _saveState.value = SaveState.IDLE
    }
}
