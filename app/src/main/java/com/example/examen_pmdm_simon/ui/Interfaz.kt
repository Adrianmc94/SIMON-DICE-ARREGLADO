package com.example.examen_pmdm_simon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Text(text = "Récord: ${viewModel.recordEnMemoria}", fontSize = 20.sp)
        Text(text = "Ronda actual: ${viewModel.ronda}", fontSize = 34.sp)

        Spacer(modifier = Modifier.height(30.dp))

        // Botones en cuadrícula 2x2
        Row {
            BotonColor(Colores.VERDE, viewModel)
            BotonColor(Colores.ROJO, viewModel)
        }
        Row {
            BotonColor(Colores.AMARILLO, viewModel)
            BotonColor(Colores.AZUL, viewModel)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Botón de control (Jugar / Reiniciar)
        if (viewModel.estadoActual == EstadoJuego.INICIO || viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Button(
                onClick = { viewModel.iniciarJuego() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = if (viewModel.estadoActual == EstadoJuego.INICIO) "EMPEZAR" else "REINTENTAR")
            }
        }

        if (viewModel.estadoActual == EstadoJuego.GAME_OVER) {
            Text(text = "¡TE HAS EQUIVOCADO!", color = androidx.compose.ui.graphics.Color.Red)
        }
    }
}

@Composable
fun BotonColor(colorEnum: Colores, viewModel: MyViewModel) {
    // Si el color actual es el que tiene que brillar, usamos opacidad completa, si no, media.
    val alpha = if (viewModel.colorIluminado == colorEnum) 1f else 0.3f

    Button(
        onClick = { viewModel.respuestaUsuario(colorEnum) },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorEnum.colorReal.copy(alpha = alpha)
        ),
        modifier = Modifier
            .size(140.dp)
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {}
}