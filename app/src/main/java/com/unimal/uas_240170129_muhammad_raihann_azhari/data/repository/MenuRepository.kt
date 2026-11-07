package com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.local.BekalDbHelper
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu

/** Operasi CRUD untuk entitas [BekalMenu]. Semua fungsi bersifat blocking: panggil dari thread background. */
class MenuRepository(context: Context) {

    private val helper = BekalDbHelper.get(context)

    fun getAll(userId: Long): List<BekalMenu> {
        val result = mutableListOf<BekalMenu>()
        helper.readableDatabase.query(
            BekalDbHelper.TABLE_MENUS, null,
            "${BekalDbHelper.COL_USER_ID} = ?", arrayOf(userId.toString()),
            null, null,
            "${BekalDbHelper.COL_DAY} ASC, ${BekalDbHelper.COL_CATEGORY} ASC, ${BekalDbHelper.COL_CREATED_AT} DESC"
        ).use { c -> while (c.moveToNext()) result.add(c.toMenu()) }
        return result
    }

    fun getById(id: Long, userId: Long): BekalMenu? =
        helper.readableDatabase.query(
            BekalDbHelper.TABLE_MENUS, null,
            "${BekalDbHelper.COL_ID} = ? AND ${BekalDbHelper.COL_USER_ID} = ?",
            arrayOf(id.toString(), userId.toString()),
            null, null, null
        ).use { c -> if (c.moveToFirst()) c.toMenu() else null }

    fun getByDay(userId: Long, day: Int): List<BekalMenu> {
        val result = mutableListOf<BekalMenu>()
        helper.readableDatabase.query(
            BekalDbHelper.TABLE_MENUS, null,
            "${BekalDbHelper.COL_USER_ID} = ? AND ${BekalDbHelper.COL_DAY} = ?",
            arrayOf(userId.toString(), day.toString()),
            null, null, "${BekalDbHelper.COL_CATEGORY} ASC"
        ).use { c -> while (c.moveToNext()) result.add(c.toMenu()) }
        return result
    }

    fun insert(menu: BekalMenu): Long =
        helper.writableDatabase.insertOrThrow(BekalDbHelper.TABLE_MENUS, null, menu.toValues())

    fun update(menu: BekalMenu): Int =
        helper.writableDatabase.update(
            BekalDbHelper.TABLE_MENUS, menu.toValues(),
            "${BekalDbHelper.COL_ID} = ? AND ${BekalDbHelper.COL_USER_ID} = ?",
            arrayOf(menu.id.toString(), menu.userId.toString())
        )

    fun delete(id: Long, userId: Long): Int =
        helper.writableDatabase.delete(
            BekalDbHelper.TABLE_MENUS,
            "${BekalDbHelper.COL_ID} = ? AND ${BekalDbHelper.COL_USER_ID} = ?",
            arrayOf(id.toString(), userId.toString())
        )

    fun setPacked(id: Long, userId: Long, packed: Boolean): Int {
        val values = ContentValues().apply { put(BekalDbHelper.COL_PACKED, if (packed) 1 else 0) }
        return helper.writableDatabase.update(
            BekalDbHelper.TABLE_MENUS, values,
            "${BekalDbHelper.COL_ID} = ? AND ${BekalDbHelper.COL_USER_ID} = ?",
            arrayOf(id.toString(), userId.toString())
        )
    }

    private fun BekalMenu.toValues() = ContentValues().apply {
        put(BekalDbHelper.COL_USER_ID, userId)
        put(BekalDbHelper.COL_NAME, name)
        put(BekalDbHelper.COL_CATEGORY, category)
        put(BekalDbHelper.COL_DAY, day)
        put(BekalDbHelper.COL_INGREDIENTS, ingredients)
        put(BekalDbHelper.COL_CALORIES, calories)
        put(BekalDbHelper.COL_PREP_MINUTES, prepMinutes)
        put(BekalDbHelper.COL_NOTES, notes)
        put(BekalDbHelper.COL_PHOTO_PATH, photoPath)
        put(BekalDbHelper.COL_PACKED, if (packed) 1 else 0)
        put(BekalDbHelper.COL_CREATED_AT, createdAt)
    }

    private fun Cursor.toMenu() = BekalMenu(
        id = getLong(getColumnIndexOrThrow(BekalDbHelper.COL_ID)),
        userId = getLong(getColumnIndexOrThrow(BekalDbHelper.COL_USER_ID)),
        name = getString(getColumnIndexOrThrow(BekalDbHelper.COL_NAME)),
        category = getInt(getColumnIndexOrThrow(BekalDbHelper.COL_CATEGORY)),
        day = getInt(getColumnIndexOrThrow(BekalDbHelper.COL_DAY)),
        ingredients = getString(getColumnIndexOrThrow(BekalDbHelper.COL_INGREDIENTS)),
        calories = getInt(getColumnIndexOrThrow(BekalDbHelper.COL_CALORIES)),
        prepMinutes = getInt(getColumnIndexOrThrow(BekalDbHelper.COL_PREP_MINUTES)),
        notes = getString(getColumnIndexOrThrow(BekalDbHelper.COL_NOTES)) ?: "",
        photoPath = getString(getColumnIndexOrThrow(BekalDbHelper.COL_PHOTO_PATH)),
        packed = getInt(getColumnIndexOrThrow(BekalDbHelper.COL_PACKED)) == 1,
        createdAt = getLong(getColumnIndexOrThrow(BekalDbHelper.COL_CREATED_AT))
    )
}
