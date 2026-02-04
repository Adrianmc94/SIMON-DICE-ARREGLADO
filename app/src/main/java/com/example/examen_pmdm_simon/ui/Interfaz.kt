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
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "RÉCORD MÁXIMO SQLITE: ${viewModel.recordMaximoSQLite}", fontSize = 18.sp, color = Color.Blue)
                Text(text = "Puntos: ${viewModel.ronda}", fontSize = 32.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Text(text = "¡FIN DEL JUEGO!", color = Color.Red, fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }

        if (viewModel.estadoActual == EstadoJuego.INICIO || viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Button(
                onClick = { viewModel.iniciarJuego() },
                modifier = Modifier.width(200.dp)
            ) {
                Text(text = if (viewModel.estadoActual == EstadoJuego.INICIO) "EMPEZAR" else "REINTENTAR")
            }
        }
    }
}

@Composable
fun BotonColor(colorEnum: Colores, viewModel: MyViewModel) {
    val alpha = if (viewModel.colorIluminado == colorEnum) 1f else 0.4f
    Button(
        onClick = { viewModel.respuestaUsuario(colorEnum) },
        colors = ButtonDefaults.buttonColors(containerColor = colorEnum.colorReal.copy(alpha = alpha)),
        modifier = Modifier.size(130.dp).padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {}
}