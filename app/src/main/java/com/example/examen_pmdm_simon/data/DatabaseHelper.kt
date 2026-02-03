package com.example.examen_pmdm_simon.data.local.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(SQL_CREATE_ENTRIES)

        /** * EXAMEN: NUEVA TABLA
         * db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY, nombre TEXT)")
         */
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SimonDice.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${PartidasContrato.PartidaEntry.TABLE_NAME} (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${PartidasContrato.PartidaEntry.COLUMN_NOMBRE} TEXT," +
                    "${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION} INTEGER," +
                    "${PartidasContrato.PartidaEntry.COLUMN_FECHA} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${PartidasContrato.PartidaEntry.TABLE_NAME}"
    }
}