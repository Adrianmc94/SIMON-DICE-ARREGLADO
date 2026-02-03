package com.example.examen_pmdm_simon.data

import androidx.compose.ui.graphics.Color

// Los colores del Simon con su valor visual asociado
enum class Colores(val colorReal: Color) {
    VERDE(Color.Green),
    ROJO(Color.Red),
    AMARILLO(Color.Yellow),
    AZUL(Color.Blue)
}

// Para saber qué está pasando en la app en todo momento
enum class EstadoJuego {
    INICIO,          // Pantalla de bienvenida
    GENERANDO,       // Simon está pensando el siguiente color
    REPRODUCIENDO,   // Los botones brillan en orden
    ESPERANDO,       // Turno del jugador
    GAME_OVER        // El jugador se ha equivocado
}

object Constantes {
    const val VELOCIDAD_MUESTRA = 600L
    const val PAUSA_ENTRE_COLORES = 200L
}