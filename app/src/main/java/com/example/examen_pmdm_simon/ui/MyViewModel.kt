package com.example.examen_pmdm_simon.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_pmdm_simon.data.*
import com.example.examen_pmdm_simon.data.local.room.AppDatabase
import com.example.examen_pmdm_simon.data.local.room.PartidaEntity
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
    private val KEY_FECHA = "fecha_score"
    private val KEY_NOMBRE = "nombre_record"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dbHelper = DatabaseHelper(application)
    private val roomDb = AppDatabase.getDatabase(application)
    private val partidaDao = roomDb.partidaDao()

    // Variable para cambiar el nombre del jugador manualmente antes de arrancar la app
    private val nombreActualSesion = "rompeEsquemas3000"

    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0)
    var fechaRecord by mutableStateOf("-")
    var nombreJugadorRecord by mutableStateOf("-")
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    init {
        recordEnMemoria = sharedPrefs.getInt(KEY_RECORD, 0)
        fechaRecord = sharedPrefs.getString(KEY_FECHA, "N/A") ?: "N/A"
        nombreJugadorRecord = sharedPrefs.getString(KEY_NOMBRE, "N/A") ?: "N/A"
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
                    actualizarRecordYFecha()
                }
                siguienteRonda()
            }
        } else {
            estadoActual = EstadoJuego.GAME_OVER
            guardarEnBasesDeDatos()
        }
    }

    private fun actualizarRecordYFecha() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        recordEnMemoria = ronda
        fechaRecord = fechaActual
        nombreJugadorRecord = nombreActualSesion

        sharedPrefs.edit()
            .putInt(KEY_RECORD, recordEnMemoria)
            .putString(KEY_FECHA, fechaRecord)
            .putString(KEY_NOMBRE, nombreJugadorRecord)
            .apply()

        Log.d("ROOM-EXAMEN", "Nuevo record: $nombreJugadorRecord, tienes $recordEnMemoria puntos!!!!!!!!!!!!!")
    }

    private fun guardarEnBasesDeDatos() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // SQLite
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, nombreActualSesion)
                    put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, ronda)
                    put(PartidasContrato.PartidaEntry.COLUMN_FECHA, fechaActual)
                }
                db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)
            } catch (e: Exception) {
                Log.e("SIMON_ERROR", "Error SQLite: ${e.message}")
            }
        }

        // Room
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val partidaRoom = PartidaEntity(
                    nombre = nombreActualSesion,
                    puntos = ronda,
                    fecha = fechaActual
                )
                partidaDao.insertar(partidaRoom)
                Log.d("ROOM-EXAMEN", "Partida guardada en Room: $partidaRoom")
            } catch (e: Exception) {
                Log.e("SIMON_ERROR", "Error Room: ${e.message}")
            }
        }
    }
}