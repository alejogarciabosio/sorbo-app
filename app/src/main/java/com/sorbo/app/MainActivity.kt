// Raíz de la app · única Activity (arquitectura Single-Activity): aloja el tema y la navegación de Compose.
package com.sorbo.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.sorbo.app.ui.navegacion.AppSorbo
import com.sorbo.app.ui.theme.SorboTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** El enlace sorbo://recuperar que abrió la app; la navegación lo consume una sola vez. */
    private var enlace by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solo al abrir en frío: si Android está restaurando la app, un formulario abierto puede estar usando una foto.
        if (savedInstanceState == null) {
            val contenedor = (application as SorboApplication).container
            lifecycleScope.launch { contenedor.limpiarFotosHuerfanas() }
            enlace = enlaceDe(intent)
        }
        enableEdgeToEdge()
        setContent {
            SorboTheme {
                AppSorbo(
                    contenedor = (application as SorboApplication).container,
                    enlace = enlace,
                    alConsumirEnlace = { enlace = null },
                )
            }
        }
    }

    /** Con la app ya abierta, el enlace llega acá (la Activity es singleTop). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        enlaceDe(intent)?.let { enlace = it }
    }

    private fun enlaceDe(intent: Intent?): Uri? = intent?.data?.takeIf { it.scheme == "sorbo" && it.host == "recuperar" }
}
