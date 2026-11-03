package com.sorbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.sorbo.app.ui.galeria.GaleriaComponentes
import com.sorbo.app.ui.theme.SorboTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SorboTheme {
                // Provisorio: se reemplaza por la navegación en el paso siguiente.
                GaleriaComponentes(modifier = Modifier.safeDrawingPadding())
            }
        }
    }
}
