package com.example.examen_pmdm_simon.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_partidas_room")
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val puntos: Int,
    val fecha: String
)