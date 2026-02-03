package com.example.examen_pmdm_simon.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_pmdm_simon.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {

    // --- CONFIGURACIÓN SHAREDPREFERENCES ---
    // RUTA: /data/data/com.example.examen_pmdm_simon/shared_prefs/simon_prefs.xml
    private val PREFS_NAME = "simon_prefs"
    private val KEY_RECORD = "max_score"
    private val KEY_FECHA = "fecha_score"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- ESTADOS REACTIVOS ---
    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0)
    var fechaRecord by mutableStateOf("-")
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    init {
        // CARGAR: Se ejecuta al iniciar la App para recuperar el récord previo.
        recordEnMemoria = sharedPrefs.getInt(KEY_RECORD, 0)
        fechaRecord = sharedPrefs.getString(KEY_FECHA, "N/A") ?: "N/A"
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
                // ACTUALIZACIÓN EN TIEMPO REAL: Si supera el récord mientras juega.
                if (ronda > recordEnMemoria) {
                    actualizarPersistencia()
                }
                siguienteRonda()
            }
        } else {
            estadoActual = EstadoJuego.GAME_OVER
        }
    }

    // ACTUALIZAR/CREAR: Guarda físicamente los datos en el XML.
    private fun actualizarPersistencia() {
        recordEnMemoria = ronda

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaActual = sdf.format(Date())
        fechaRecord = fechaActual

        // GUARDADO FÍSICO: Se abre el editor, se ponen los datos y se aplica.
        with(sharedPrefs.edit()) {
            putInt(KEY_RECORD, recordEnMemoria) // Crea o actualiza el entero
            putString(KEY_FECHA, fechaActual)    // Crea o actualiza el String
            apply()
        }
    }
}