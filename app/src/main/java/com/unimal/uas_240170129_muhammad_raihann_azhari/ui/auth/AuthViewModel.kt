package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.auth

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.User
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.AuthRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.EmailAlreadyUsedException
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.NetworkUnavailableException
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val user: User) : AuthState
    data class Error(
        @StringRes val messageRes: Int,
        val emailError: Boolean = false,
        val detail: String? = null
    ) : AuthState
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(app)
    private val session = SessionManager(app)

    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    fun login(email: String, password: String) {
        _state.value = AuthState.Loading
        AppExecutors.io.execute {
            try {
                val user = repo.login(email, password)
                if (user == null) {
                    _state.postValue(AuthState.Error(R.string.error_login_failed))
                } else {
                    session.save(user)
                    _state.postValue(AuthState.Success(user))
                }
            } catch (e: NetworkUnavailableException) {
                _state.postValue(AuthState.Error(R.string.error_network))
            } catch (e: Exception) {
                Log.e("BekalAuth", "Auth gagal", e)
                _state.postValue(AuthState.Error(R.string.error_generic, detail = describe(e)))
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        _state.value = AuthState.Loading
        AppExecutors.io.execute {
            try {
                val user = repo.register(name, email, password)
                session.save(user)
                _state.postValue(AuthState.Success(user))
            } catch (e: EmailAlreadyUsedException) {
                _state.postValue(AuthState.Error(R.string.error_email_taken, emailError = true))
            } catch (e: NetworkUnavailableException) {
                _state.postValue(AuthState.Error(R.string.error_network))
            } catch (e: Exception) {
                Log.e("BekalAuth", "Auth gagal", e)
                _state.postValue(AuthState.Error(R.string.error_generic, detail = describe(e)))
            }
        }
    }

    fun reset() {
        _state.value = AuthState.Idle
    }

    /** Ringkasan penyebab (kode/pesan Firebase) untuk membantu pelacakan masalah. */
    private fun describe(e: Exception): String {
        val root = e.cause ?: e
        val code = (root as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
        return listOfNotNull(code, root.message).joinToString(": ").take(160)
    }
}