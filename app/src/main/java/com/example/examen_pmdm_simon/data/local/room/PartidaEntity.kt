package com.example.examen_pmdm_simon.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_partidas_room")
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "puntos") val puntos: Int,
    @ColumnInfo(name = "fecha") val fecha: String
)