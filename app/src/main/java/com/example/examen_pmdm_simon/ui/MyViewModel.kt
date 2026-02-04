package com.example.examen_pmdm_simon.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_pmdm_simon.data.*
import com.example.examen_pmdm_simon.data.local.sqlite.DatabaseHelper
import com.example.examen_pmdm_simon.data.local.sqlite.PartidasContrato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {

    private val PREFS_NAME = "simon_prefs"
    private val KEY_RECORD = "max_score"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dbHelper = DatabaseHelper(application)

    var recordMaximoSQLite by mutableStateOf(0)
    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0)
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    init {
        recordEnMemoria = sharedPrefs.getInt(KEY_RECORD, 0)
        actualizarRecordMaximoDesdeBD()
    }

    private fun actualizarRecordMaximoDesdeBD() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT MAX(${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION}) FROM ${PartidasContrato.PartidaEntry.TABLE_NAME}", null)
            if (cursor.moveToFirst()) {
                recordMaximoSQLite = cursor.getInt(0)
            }
            cursor.close()
        }
    }

    fun iniciarJuego() {
        secuenciaSimon.clear()
        ronda = 0
        siguienteRonda()
    }

    private fun siguienteRonda() {
        indiceUsuario = 0
        ronda++
        secuenciaSimon.add(Colores.values().random())
        reproducirSecuencia()
    }

    private fun reproducirSecuencia() {
        viewModelScope.launch {
            estadoActual = EstadoJuego.REPRODUCIENDO
            delay(500)
            for (color in secuenciaSimon) {
                colorIluminado = color
                delay(Constantes.VELOCIDAD_MUESTRA)
                colorIluminado = null
                delay(Constantes.PAUSA_ENTRE_COLORES)
            }
            estadoActual = EstadoJuego.ESPERANDO
        }
    }

    fun respuestaUsuario(colorPulsado: Colores) {
        if (estadoActual != EstadoJuego.ESPERANDO) return
        if (colorPulsado == secuenciaSimon[indiceUsuario]) {
            indiceUsuario++
            if (indiceUsuario == secuenciaSimon.size) {
                if (ronda > recordEnMemoria) {
                    recordEnMemoria = ronda
                    sharedPrefs.edit().putInt(KEY_RECORD, recordEnMemoria).apply()
                }
                siguienteRonda()
            }
        } else {
            estadoActual = EstadoJuego.GAME_OVER
            guardarEnSQLite()
        }
    }

    private fun guardarEnSQLite() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, "Player_Examen")
                put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, ronda)
                put(PartidasContrato.PartidaEntry.COLUMN_FECHA, fechaActual)
            }

            val newRowId = db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)

            val consultaTop10 = "SELECT id FROM ${PartidasContrato.PartidaEntry.TABLE_NAME} " +
                    "ORDER BY ${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION} DESC, " +
                    "${PartidasContrato.PartidaEntry.COLUMN_FECHA} ASC LIMIT 10"

            val cursor = db.rawQuery(consultaTop10, null)
            val idsTop10 = mutableListOf<Long>()
            var esTop10 = false

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                idsTop10.add(id)
                if (id == newRowId) {
                    esTop10 = true
                }
            }
            cursor.close()

            if (esTop10) {
                Log.d("SQLITE", "formas parte de los diez primeros")
            }

            val idsString = idsTop10.joinToString(",")
            db.execSQL("DELETE FROM ${PartidasContrato.PartidaEntry.TABLE_NAME} WHERE id NOT IN ($idsString)")

            actualizarRecordMaximoDesdeBD()
        }
    }
}