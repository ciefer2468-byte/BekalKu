package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.MenuRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import java.io.File

class DetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MenuRepository(app)
    private val session = SessionManager(app)

    private val _menu = MutableLiveData<BekalMenu?>()
    val menu: LiveData<BekalMenu?> = _menu

    private val _deleted = MutableLiveData<Boolean>()
    val deleted: LiveData<Boolean> = _deleted

    fun load(id: Long) {
        val userId = session.userId
        AppExecutors.io.execute {
            val result = try {
                repo.getById(id, userId)
            } catch (e: Exception) {
                null
            }
            _menu.postValue(result)
        }
    }

    fun togglePacked() {
        val current = _menu.value ?: return
        val updated = current.copy(packed = !current.packed)
        _menu.value = updated
        val userId = session.userId
        AppExecutors.io.execute {
            try {
                repo.setPacked(updated.id, userId, updated.packed)
            } catch (e: Exception) {
                _menu.postValue(current)
            }
        }
    }

    fun delete() {
        val current = _menu.value ?: return
        val userId = session.userId
        AppExecutors.io.execute {
            try {
                repo.delete(current.id, userId)
                current.photoPath?.let { File(it).delete() }
                _deleted.postValue(true)
            } catch (e: Exception) {
                _deleted.postValue(false)
            }
        }
    }
}
