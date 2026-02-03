package com.example.examen_pmdm_simon.data

import android.provider.BaseColumns

// EXAMEN: Define "Columnas y tablas".
object PartidasContrato {
    object PartidaEntry : BaseColumns {
        // Nombre de la tabla física en el archivo .db
        const val TABLE_NAME = "historial"

        // Definición de las columnas
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_PUNTUACION = "puntuacion"
        const val COLUMN_FECHA = "fecha"
    }
}