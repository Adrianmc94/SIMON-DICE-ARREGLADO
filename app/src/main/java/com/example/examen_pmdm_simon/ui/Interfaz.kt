package com.example.examen_pmdm_simon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.examen_pmdm_simon.data.Colores
import com.example.examen_pmdm_simon.data.EstadoJuego

@Composable
fun PantallaSimon(viewModel: MyViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cabecera de Récords
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TOP SCORE: ${viewModel.recordEnMemoria}", fontSize = 22.sp, color = Color.Black)
                Text(text = "Fecha: ${viewModel.fechaRecord}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Text(text = "Ronda: ${viewModel.ronda}", fontSize = 40.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Botones de colores
        Column {
            Row {
                BotonColor(Colores.VERDE, viewModel)
                BotonColor(Colores.ROJO, viewModel)
            }
            Row {
                BotonColor(Colores.AMARILLO, viewModel)
                BotonColor(Colores.AZUL, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Estados y controles
        if (viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Text(text = "¡GAME OVER!", color = Color.Red, fontSize = 24.sp)
        }

        if (viewModel.estadoActual == EstadoJuego.INICIO || viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Button(onClick = { viewModel.iniciarJuego() }) {
                Text(text = if (viewModel.estadoActual == EstadoJuego.INICIO) "JUGAR" else "REINTENTAR")
            }
        }
    }
}

@Composable
fun BotonColor(colorEnum: Colores, viewModel: MyViewModel) {
    val alpha = if (viewModel.colorIluminado == colorEnum) 1f else 0.3f

    Button(
        onClick = { viewModel.respuestaUsuario(colorEnum) },
        colors = ButtonDefaults.buttonColors(containerColor = colorEnum.colorReal.copy(alpha = alpha)),
        modifier = Modifier.size(130.dp).padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {}
}