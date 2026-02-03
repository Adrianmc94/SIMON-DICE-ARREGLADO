package com.example.examen_pmdm_simon.data

import android.provider.BaseColumns

object PartidasContrato {
    // Definimos el contenido de la tabla
    object PartidaEntry : BaseColumns {
        const val TABLE_NAME = "historial"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_PUNTUACION = "puntuacion"
        const val COLUMN_FECHA = "fecha"
    }
}