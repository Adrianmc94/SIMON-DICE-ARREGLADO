package com.example.examen_pmdm_simon.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_pmdm_simon.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    // ESTADOS REACTIVOS: Compose "observa" estas variables. Si cambian, la UI se repinta sola.
    var ronda by mutableStateOf(0)
    var recordEnMemoria by mutableStateOf(0) // Se inicializará con SharedPreferences
    var estadoActual by mutableStateOf(EstadoJuego.INICIO)
    var colorIluminado by mutableStateOf<Colores?>(null)

    // Lógica interna: no visible para la Interfaz (UI).
    private val secuenciaSimon = mutableListOf<Colores>()
    private var indiceUsuario = 0

    // INICIAR: Resetea la partida.
    fun iniciarJuego() {
        secuenciaSimon.clear()
        ronda = 0
        siguienteRonda()
    }

    // PASO DE RONDA: Incrementa dificultad y genera color aleatorio.
    private fun siguienteRonda() {
        indiceUsuario = 0
        ronda++
        secuenciaSimon.add(Colores.values().random())
        reproducirSecuencia()
    }

    // CORRUTINA DE MUESTRA: Ilumina los botones uno a uno.
    // IMPORTANTE: Se usa viewModelScope para que el proceso sea asíncrono.
    private fun reproducirSecuencia() {
        viewModelScope.launch {
            estadoActual = EstadoJuego.REPRODUCIENDO
            delay(500)
            for (color in secuenciaSimon) {
                colorIluminado = color // Ilumina botón
                delay(Constantes.VELOCIDAD_MUESTRA)
                colorIluminado = null  // Apaga botón
                delay(Constantes.PAUSA_ENTRE_COLORES)
            }
            estadoActual = EstadoJuego.ESPERANDO // Devuelve el control al usuario
        }
    }

    // RESPUESTA USUARIO: Compara lo pulsado con la secuencia guardada.
    fun respuestaUsuario(colorPulsado: Colores) {
        // Bloqueo de seguridad: si no es el turno del usuario, ignoramos el click.
        if (estadoActual != EstadoJuego.ESPERANDO) return

        if (colorPulsado == secuenciaSimon[indiceUsuario]) {
            indiceUsuario++
            if (indiceUsuario == secuenciaSimon.size) {
                actualizarRecord()
                siguienteRonda()
            }
        } else {
            // el guardado en SharedPreferences, SQLite y Room.
            estadoActual = EstadoJuego.GAME_OVER
        }
    }

    private fun actualizarRecord() {
        if (ronda > recordEnMemoria) {
            recordEnMemoria = ronda
        }
    }
}