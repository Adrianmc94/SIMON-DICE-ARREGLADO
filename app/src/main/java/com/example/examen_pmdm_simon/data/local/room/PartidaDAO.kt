package com.example.examen_pmdm_simon.data.local.room

import androidx.room.*
import com.example.examen_pmdm_simon.data.local.room.PartidaEntity

@Dao
interface PartidaDao {
    @Insert
    suspend fun insertar(partida: PartidaEntity)

    @Query("SELECT * FROM tabla_partidas_room ORDER BY puntos DESC")
    suspend fun obtenerRanking(): List<PartidaEntity>

    @Query("DELETE FROM tabla_partidas_room")
    suspend fun borrarTodo()
}