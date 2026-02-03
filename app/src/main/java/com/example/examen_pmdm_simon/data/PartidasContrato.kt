package com.example.examen_pmdm_simon.data.local.sqlite

import android.provider.BaseColumns

object PartidasContrato {
    object PartidaEntry : BaseColumns {
        const val TABLE_NAME = "historial"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_PUNTUACION = "puntuacion"
        const val COLUMN_FECHA = "fecha"

        /** * EXAMEN: Ejemplo de cómo añadir una columna extra si lo piden
         * const val COLUMN_NIVEL = "nivel"
         */
    }
}