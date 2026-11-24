// Raíz de la app · única Activity (arquitectura Single-Activity): aloja el tema y la navegación de Compose.
package com.sorbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.sorbo.app.ui.navegacion.AppSorbo
import com.sorbo.app.ui.theme.SorboTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solo al abrir en frío: si Android está restaurando la app, un formulario abierto puede estar usando una foto.
        if (savedInstanceState == null) {
            val contenedor = (application as SorboApplication).container
            lifecycleScope.launch { contenedor.limpiarFotosHuerfanas() }
        }
        enableEdgeToEdge()
        setContent {
            SorboTheme {
                AppSorbo((application as SorboApplication).container)
            }
        }
    }
}
