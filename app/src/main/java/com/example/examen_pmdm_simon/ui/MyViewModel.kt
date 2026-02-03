package com.example.examen_pmdm_simon.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_pmdm_simon.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {

    // --- CONFIGURACIÓN DE PERSISTENCIA ---

    // SharedPreferences (Récord simple)
    private val PREFS_NAME = "simon_prefs"
    private val KEY_RECORD = "max_score"
    private val KEY_FECHA = "fecha_score"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // SQLite (Historial completo)
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
        // Cargar el récord de SharedPreferences al arrancar
        recordEnMemoria = sharedPrefs.getInt(KEY_RECORD, 0)
        fechaRecord = sharedPrefs.getString(KEY_FECHA, "N/A") ?: "N/A"

        // Ver historial actual en Logcat al iniciar
        leerPartidasDeSQLite()
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
                siguienteRonda()
            }
        } else {
            // EL JUGADOR PIERDE
            estadoActual = EstadoJuego.GAME_OVER
            gestionarFinPartida()
        }
    }

    // --- GESTIÓN DE PERSISTENCIA ---

    private fun gestionarFinPartida() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // SharedPreferences: Solo si es nuevo récord
        if (ronda > recordEnMemoria) {
            recordEnMemoria = ronda
            fechaRecord = fechaActual
            with(sharedPrefs.edit()) {
                putInt(KEY_RECORD, recordEnMemoria)
                putString(KEY_FECHA, fechaRecord)
                apply()
            }
            Log.d("SIMON", "Nuevo récord guardado en SharedPreferences")
        }

        // SQLite: Guardamos SIEMPRE la partida en el historial
        insertarPartidaEnSQLite("Jugador_Examen", ronda, fechaActual)
    }
    // Operación: INSERT
    private fun insertarPartidaEnSQLite(nombre: String, puntos: Int, fecha: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, nombre)
            put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, puntos)
            put(PartidasContrato.PartidaEntry.COLUMN_FECHA, fecha)
        }
        val newRowId = db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)
        Log.d("SQLITE", "Partida insertada en historial. ID: $newRowId")

        // Leemos después de insertar para comprobar en Logcat
        leerPartidasDeSQLite()
    }

    // Operación: SELECT
    fun leerPartidasDeSQLite() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            PartidasContrato.PartidaEntry.TABLE_NAME,
            null, null, null, null, null,
            "${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION} DESC" // Ordenar por nota
        )

        Log.d("SQLITE", "--- HISTORIAL DE PARTIDAS ---")
        with(cursor) {
            while (moveToNext()) {
                val nombre = getString(getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_NOMBRE))
                val puntos = getInt(getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION))
                val fecha = getString(getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_FECHA))
                Log.d("SQLITE", "Jugador: $nombre | Puntos: $puntos | Fecha: $fecha")
            }
        }
        cursor.close()
    }

    // Operación: UPDATE
    fun actualizarNombreUltimaPartida(nuevoNombre: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, nuevoNombre)
        }
        // Actualiza todas las partidas de "Jugador_Examen"
        db.update(PartidasContrato.PartidaEntry.TABLE_NAME, values, "nombre = ?", arrayOf("Jugador_Examen"))
    }
}