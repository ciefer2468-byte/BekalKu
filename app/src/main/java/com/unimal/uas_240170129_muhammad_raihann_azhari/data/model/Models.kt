package com.unimal.uas_240170129_muhammad_raihann_azhari.data.model

/** Pengguna aplikasi (akun lokal). */
data class User(
    val id: Long,
    val name: String,
    val email: String
)

/**
 * Entitas utama: satu menu bekal yang direncanakan.
 * [category] dan [day] disimpan sebagai indeks (lihat array di strings.xml).
 */
data class BekalMenu(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val category: Int,
    val day: Int,
    val ingredients: String,
    val calories: Int,
    val prepMinutes: Int,
    val notes: String = "",
    val photoPath: String? = null,
    val packed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
