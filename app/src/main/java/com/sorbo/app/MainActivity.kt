// Raíz de la app · única Activity (arquitectura Single-Activity): aloja el tema y la navegación de Compose.
package com.sorbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sorbo.app.ui.navegacion.AppSorbo
import com.sorbo.app.ui.theme.SorboTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SorboTheme {
                AppSorbo((application as SorboApplication).container)
            }
        }
    }
}
