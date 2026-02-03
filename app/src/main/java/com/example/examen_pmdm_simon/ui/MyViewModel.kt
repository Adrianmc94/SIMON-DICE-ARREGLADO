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


    // --- OPERACIONES SQLITE


    // Operación: INSERT
    // "Varios records". Se guarda cada partida terminada.
    private fun insertarPartidaEnSQLite(nombre: String, puntos: Int, fecha: String) {
        val db = dbHelper.writableDatabase // Abrir en modo escritura

        // ContentValues empaqueta los datos para la fila
        val values = ContentValues().apply {
            put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, nombre)
            put(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION, puntos)
            put(PartidasContrato.PartidaEntry.COLUMN_FECHA, fecha)
        }

        // db.insert devuelve el ID de la nueva fila (o -1 si hay error)
        val newRowId = db.insert(PartidasContrato.PartidaEntry.TABLE_NAME, null, values)
        Log.d("SQLITE", "Partida insertada. ID: $newRowId")
    }

    // SELECT: "getAll" y "getMax".
    fun leerPartidasDeSQLite() {
        val db = dbHelper.readableDatabase // Abrir en modo lectura

        // Consultamos la tabla. El último parámetro es ORDER BY.
        val cursor = db.query(
            PartidasContrato.PartidaEntry.TABLE_NAME,
            null, null, null, null, null,
            "${PartidasContrato.PartidaEntry.COLUMN_PUNTUACION} DESC" // Ordenar por puntuación mayor
        )

        Log.d("SQLITE", "--- HISTORIAL ---")
        with(cursor) {
            while (moveToNext()) {
                // Extraer datos usando los nombres de las columnas del Contrato
                val nombre = getString(getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_NOMBRE))
                val puntos = getInt(getColumnIndexOrThrow(PartidasContrato.PartidaEntry.COLUMN_PUNTUACION))
                Log.d("SQLITE", "Jugador: $nombre | Puntos: $puntos")
            }
        }
        cursor.close() // IMPORTANTE: Siempre cerrar el cursor para liberar memoria.
    }

    // UPDATE: "Actualizar". Ejemplo de cambiar el nombre del jugador.
    fun actualizarNombreUltimaPartida(nuevoNombre: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(PartidasContrato.PartidaEntry.COLUMN_NOMBRE, nuevoNombre)
        }
        // Cláusula WHERE: "Actualizar donde nombre sea Jugador_Examen"
        db.update(PartidasContrato.PartidaEntry.TABLE_NAME, values, "nombre = ?", arrayOf("Jugador_Examen"))
    }

    // DELETE: "Borrar historial".
    fun borrarHistorialSQLite() {
        val db = dbHelper.writableDatabase
        db.delete(PartidasContrato.PartidaEntry.TABLE_NAME, null, null)
    }
}