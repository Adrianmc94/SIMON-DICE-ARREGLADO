package com.example.examen_pmdm_simon.data

import androidx.compose.ui.graphics.Color

// ENUM DE COLORES: Asocia el nombre del color con su valor visual de Compose.
enum class Colores(val colorReal: Color) {
    VERDE(Color.Green),
    ROJO(Color.Red),
    AMARILLO(Color.Yellow),
    AZUL(Color.Blue)
}

// ESTADOS DEL JUEGO: Controlan el flujo de la aplicación (MVVM).
// INICIO: Antes de empezar. GENERANDO: Simón crea la secuencia.
// REPRODUCIENDO: Se muestra la secuencia (bloquear botones en la UI aquí).
// ESPERANDO: Turno del jugador. GAME_OVER: Fin del juego (punto clave para guardar datos).
enum class EstadoJuego {
    INICIO,
    GENERANDO,
    REPRODUCIENDO,
    ESPERANDO,
    GAME_OVER
}

// CONSTANTES: Centraliza los tiempos para cambiarlos rápido (Dificultad).
object Constantes {
    const val VELOCIDAD_MUESTRA = 600L
    const val PAUSA_ENTRE_COLORES = 200L
}