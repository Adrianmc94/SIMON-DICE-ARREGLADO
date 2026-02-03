package com.example.examen_pmdm_simon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.examen_pmdm_simon.ui.MyViewModel
import com.example.examen_pmdm_simon.ui.PantallaSimon

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val simonViewModel: MyViewModel by viewModels()

        setContent {
            PantallaSimon(simonViewModel)
        }
    }
}