package com.example.examen_pmdm_simon.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
// IMPORTANTE: Verifica que estos paquetes coincidan con el nombre de tu proyecto
import com.example.examen_pmdm_simon.data.* import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. PERSISTENCIA: SHAREDPREFERENCES ---
    private val PREFS_NAME = "simon_prefs"
    private val KEY_RECORD = "max_score"
    private val KEY_FECHA = "fecha_score"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- 2. PERSISTENCIA: SQLITE (Manual) ---
    private val dbHelper = DatabaseHelper(application)

    // --- 3. PERSISTENCIA: ROOM ---
    private val roomDb = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "simon_room_db"
    ).build()
    private val partidaDao = roomDb.partidaDao()

    // --- ESTADOS REACTIVOS ---
    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0)
    var fechaRecord by mutableStateOf("-")
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    init {
        // Cargar récord inicial de SharedPreferences
        recordEnMemoria = sharedPrefs.getInt(KEY_RECORD, 0)
        fechaRecord = sharedPrefs.getString(KEY_FECHA, "N/A") ?: "N/A"
    }

    // --- LÓGICA DEL JUEGO ---
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
                // Si el usuario acierta toda la secuencia, comprobamos récord
                if (ronda > recordEnMemoria) {
                    actualizarRecordYFecha()
                }
                siguienteRonda()
            }
        } else {
            estadoActual = EstadoJuego.GAME_OVER
            guardarEnBasesDeDatos() // Al perder, guardamos en SQLite y Room
        }
    }

    // --- GESTIÓN DE SHAREDPREFERENCES ---
    private fun actualizarRecordYFecha() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        recordEnMemoria = ronda
        fechaRecord = fechaActual

        sharedPrefs.edit()
            .putInt(KEY_RECORD, recordEnMemoria)
            .putString(KEY_FECHA, fechaRecord)
            .apply()
        Log.d("SIMON_CHECK", "SharedPreferences: Récord actualizado")
    }

    // --- GESTIÓN DE SQLITE Y ROOM ---
    private fun guardarEnBasesDeDatos() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // 1. Guardar en SQLite Manual
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, "User_SQLite")
                put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, ronda)
                put(PartidasContrato.PartidaEntry.COLUMN_FECHA, fechaActual)
            }
            db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)
            Log.d("SIMON_CHECK", "SQLite: Guardado correctamente")
        } catch (e: Exception) {
            Log.e("SIMON_ERROR", "Error en SQLite: ${e.message}")
        }

        // 2. Guardar en Room (Usando Corrutinas en hilo IO)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val partidaRoom = PartidaEntity(
                    nombre = "User_Room",
                    puntos = ronda,
                    fecha = fechaActual
                )
                partidaDao.insertar(partidaRoom)
                Log.d("SIMON_CHECK", "Room: Guardado correctamente")
            } catch (e: Exception) {
                Log.e("SIMON_ERROR", "Error en Room: ${e.message}")
            }
        }
    }
}