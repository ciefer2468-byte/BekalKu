package com.unimal.uas_240170129_muhammad_raihann_azhari.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Penyimpanan persisten lokal (SQLite). Data tetap ada setelah aplikasi ditutup. */
class BekalDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_SALT TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_MENUS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_NAME TEXT NOT NULL,
                $COL_CATEGORY INTEGER NOT NULL,
                $COL_DAY INTEGER NOT NULL,
                $COL_INGREDIENTS TEXT NOT NULL,
                $COL_CALORIES INTEGER NOT NULL,
                $COL_PREP_MINUTES INTEGER NOT NULL,
                $COL_NOTES TEXT NOT NULL DEFAULT '',
                $COL_PHOTO_PATH TEXT,
                $COL_PACKED INTEGER NOT NULL DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_menus_user ON $TABLE_MENUS($COL_USER_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENUS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "bekalku.db"
        private const val DB_VERSION = 1

        const val TABLE_USERS = "users"
        const val TABLE_MENUS = "menus"

        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD_HASH = "password_hash"
        const val COL_SALT = "salt"
        const val COL_CREATED_AT = "created_at"
        const val COL_USER_ID = "user_id"
        const val COL_CATEGORY = "category"
        const val COL_DAY = "day"
        const val COL_INGREDIENTS = "ingredients"
        const val COL_CALORIES = "calories"
        const val COL_PREP_MINUTES = "prep_minutes"
        const val COL_NOTES = "notes"
        const val COL_PHOTO_PATH = "photo_path"
        const val COL_PACKED = "packed"

        @Volatile
        private var instance: BekalDbHelper? = null

        fun get(context: Context): BekalDbHelper =
            instance ?: synchronized(this) {
                instance ?: BekalDbHelper(context).also { instance = it }
            }
    }
}
