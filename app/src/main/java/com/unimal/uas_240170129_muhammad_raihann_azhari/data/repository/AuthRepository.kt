package com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository

import android.content.ContentValues
import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.local.BekalDbHelper
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.User
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.PasswordHasher
import java.util.concurrent.ExecutionException

class EmailAlreadyUsedException : Exception()
class NetworkUnavailableException : Exception()

/**
 * Registrasi & login.
 *
 * - Bila `google-services.json` terpasang, autentikasi memakai **Firebase Authentication**.
 * - Bila tidak, memakai akun lokal (SQLite, password di-hash PBKDF2 + salt) sehingga aplikasi tetap berjalan.
 *
 * Pada kedua mode, tabel `users` lokal dipakai sebagai id pemilik data menu.
 * Semua fungsi bersifat blocking: panggil dari thread background.
 */
class AuthRepository(private val context: Context) {

    private val helper = BekalDbHelper.get(context)

    private val useFirebase: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    @Throws(EmailAlreadyUsedException::class, NetworkUnavailableException::class)
    fun register(name: String, email: String, password: String): User {
        val normalizedEmail = email.trim().lowercase()
        return if (useFirebase) {
            try {
                val auth = FirebaseAuth.getInstance()
                val result = Tasks.await(auth.createUserWithEmailAndPassword(normalizedEmail, password))
                result.user?.let { user ->
                    val profile = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                    Tasks.await(user.updateProfile(profile))
                }
                ensureLocalUser(name.trim(), normalizedEmail)
            } catch (e: ExecutionException) {
                throw mapFirebaseError(e)
            }
        } else {
            registerLocal(name.trim(), normalizedEmail, password)
        }
    }

    /** @return [User] bila kredensial benar, `null` bila salah. */
    @Throws(NetworkUnavailableException::class)
    fun login(email: String, password: String): User? {
        val normalizedEmail = email.trim().lowercase()
        return if (useFirebase) {
            try {
                val result = Tasks.await(
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(normalizedEmail, password)
                )
                val name = result.user?.displayName?.takeIf { it.isNotBlank() }
                    ?: normalizedEmail.substringBefore('@')
                ensureLocalUser(name, normalizedEmail)
            } catch (e: ExecutionException) {
                val mapped = mapFirebaseError(e)
                if (mapped is NetworkUnavailableException) throw mapped
                null
            }
        } else {
            loginLocal(normalizedEmail, password)
        }
    }

    fun signOut() {
        if (useFirebase) FirebaseAuth.getInstance().signOut()
    }

    private fun mapFirebaseError(e: ExecutionException): Exception = when (e.cause) {
        is FirebaseAuthUserCollisionException -> EmailAlreadyUsedException()
        is FirebaseNetworkException -> NetworkUnavailableException()
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> IllegalArgumentException("invalid credentials")
        else -> e
    }

    // ---------- Mode lokal ----------

    private fun registerLocal(name: String, email: String, password: String): User {
        val db = helper.writableDatabase
        db.query(
            BekalDbHelper.TABLE_USERS, arrayOf(BekalDbHelper.COL_ID),
            "${BekalDbHelper.COL_EMAIL} = ?", arrayOf(email), null, null, null
        ).use { if (it.moveToFirst()) throw EmailAlreadyUsedException() }

        val salt = PasswordHasher.newSalt()
        val values = ContentValues().apply {
            put(BekalDbHelper.COL_NAME, name)
            put(BekalDbHelper.COL_EMAIL, email)
            put(BekalDbHelper.COL_PASSWORD_HASH, PasswordHasher.hash(password, salt))
            put(BekalDbHelper.COL_SALT, salt)
            put(BekalDbHelper.COL_CREATED_AT, System.currentTimeMillis())
        }
        return User(db.insertOrThrow(BekalDbHelper.TABLE_USERS, null, values), name, email)
    }

    private fun loginLocal(email: String, password: String): User? {
        helper.readableDatabase.query(
            BekalDbHelper.TABLE_USERS, null,
            "${BekalDbHelper.COL_EMAIL} = ?", arrayOf(email), null, null, null
        ).use { c ->
            if (!c.moveToFirst()) return null
            val salt = c.getString(c.getColumnIndexOrThrow(BekalDbHelper.COL_SALT))
            val storedHash = c.getString(c.getColumnIndexOrThrow(BekalDbHelper.COL_PASSWORD_HASH))
            if (salt.isEmpty() || PasswordHasher.hash(password, salt) != storedHash) return null
            return User(
                id = c.getLong(c.getColumnIndexOrThrow(BekalDbHelper.COL_ID)),
                name = c.getString(c.getColumnIndexOrThrow(BekalDbHelper.COL_NAME)),
                email = c.getString(c.getColumnIndexOrThrow(BekalDbHelper.COL_EMAIL))
            )
        }
    }

    /** Mode Firebase: pastikan ada baris pengguna lokal (tanpa password) sebagai pemilik data menu. */
    private fun ensureLocalUser(name: String, email: String): User {
        val db = helper.writableDatabase
        db.query(
            BekalDbHelper.TABLE_USERS, arrayOf(BekalDbHelper.COL_ID),
            "${BekalDbHelper.COL_EMAIL} = ?", arrayOf(email), null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(0)
                val values = ContentValues().apply { put(BekalDbHelper.COL_NAME, name) }
                db.update(BekalDbHelper.TABLE_USERS, values, "${BekalDbHelper.COL_ID} = ?", arrayOf(id.toString()))
                return User(id, name, email)
            }
        }
        val values = ContentValues().apply {
            put(BekalDbHelper.COL_NAME, name)
            put(BekalDbHelper.COL_EMAIL, email)
            put(BekalDbHelper.COL_PASSWORD_HASH, "")
            put(BekalDbHelper.COL_SALT, "")
            put(BekalDbHelper.COL_CREATED_AT, System.currentTimeMillis())
        }
        return User(db.insertOrThrow(BekalDbHelper.TABLE_USERS, null, values), name, email)
    }
}
