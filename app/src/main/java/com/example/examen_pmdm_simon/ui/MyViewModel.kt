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

    // --- 1. CONFIGURACIÓN SHAREDPREFERENCES ---
    private val PREFS_NAME = "simon_prefs"
    private val KEY_RECORD = "max_score"
    private val KEY_FECHA = "fecha_score"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- 2. CONFIGURACIÓN SQLITE (CONTROLADOR) ---
    /** * EXAMEN: CONTROLADOR */
    private val dbHelper = DatabaseHelper(application)

    // --- ESTADOS REACTIVOS ---
    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0)
    var fechaRecord by mutableStateOf("-")
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    init {
        // CARGAR: Recupera récord de SharedPreferences al iniciar
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
                if (ronda > recordEnMemoria) {
                    actualizarPersistencia()
                }
                siguienteRonda()
            }
        } else {
            estadoActual = EstadoJuego.GAME_OVER

            // --- IMPLEMENTACIÓN EN EL CONTROLADOR (Acciones al morir) ---
            guardarTopTresShared(ronda)
            insertarEnSQLite(ronda)
            getAllPartidas() // Ver historial en Logcat
        }
    }

    private fun actualizarPersistencia() {
        recordEnMemoria = ronda
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        fechaRecord = fechaActual

        with(sharedPrefs.edit()) {
            putInt(KEY_RECORD, recordEnMemoria)
            putString(KEY_FECHA, fechaActual)
            apply()
        }
    }

    // --- BLOQUE SHAREDPREFERENCES ---
    private fun guardarTopTresShared(puntos: Int) {
        val r1 = sharedPrefs.getInt("top1", 0)
        val r2 = sharedPrefs.getInt("top2", 0)
        val r3 = sharedPrefs.getInt("top3", 0)
        val editor = sharedPrefs.edit()

        if (puntos > r1) {
            editor.putInt("top1", puntos); editor.putInt("top2", r1); editor.putInt("top3", r2)
        } else if (puntos > r2) {
            editor.putInt("top2", puntos); editor.putInt("top3", r2)
        } else if (puntos > r3) {
            editor.putInt("top3", puntos)
        }

        // Variable "ruta" solicitada
        editor.putString("ruta", "data/data/com.example/shared_prefs")
        editor.apply()
        Log.d("EXAMEN", "Variable ruta: ${sharedPrefs.getString("ruta", "error")}")
    }

    // --- BLOQUE SQLITE (TODO LO SOLICITADO) ---

    /** * EXAMEN: VARIOS RECORDS (Insertar cada partida) */
    private fun insertarEnSQLite(puntos: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, "Jugador_Examen")
                put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, puntos)
                put(PartidasContrato.PartidaEntry.COLUMN_FECHA, "03/02/2026")
            }
            db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)
            Log.d("SQLITE", "Partida guardada correctamente")
        }
    }

    /** * EXAMEN: getAll (Obtener historial completo) */
    fun getAllPartidas() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.query(PartidasContrato.PartidaEntry.TABLE_NAME, null, null, null, null, null, null)
            while (cursor.moveToNext()) {
                val pts = cursor.getInt(cursor.getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION))
                Log.d("SQLITE", "Puntos en historial: $pts")
            }
            cursor.close()
        }
    }

    /** * EXAMEN: getMax (Obtener puntuación máxima con SQL puro) */
    fun getMaxPuntuacion(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT MAX(${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION}) FROM ${PartidasContrato.PartidaEntry.TABLE_NAME}", null)
        var max = 0
        if (cursor.moveToFirst()) {
            max = cursor.getInt(0)
        }
        cursor.close()
        Log.d("SQLITE", "Récord máximo encontrado: $max")
        return max
    }

    /** * EXAMEN: getRecordById (Buscar registro específico) */
    fun getRecordById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                PartidasContrato.PartidaEntry.TABLE_NAME,
                null,
                "id = ?",
                arrayOf(id.toString()),
                null, null, null
            )
            if (cursor.moveToFirst()) {
                val p = cursor.getInt(cursor.getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION))
                Log.d("SQLITE", "Partida con ID $id tiene $p puntos")
            }
            cursor.close()
        }
    }
}