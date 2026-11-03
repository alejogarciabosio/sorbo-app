// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.FilaEstadistica
import com.sorbo.app.ui.components.FinalItemAjustes
import com.sorbo.app.ui.components.ItemAjustes
import com.sorbo.app.ui.components.ItemVino
import com.sorbo.app.ui.components.MiniaturaEtiqueta
import com.sorbo.app.ui.components.OpcionGrande
import com.sorbo.app.ui.components.TarjetaDegustacion
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.TonoEtiqueta
import com.sorbo.app.ui.components.Veredicto
import com.sorbo.app.ui.theme.EstiloMarcaDisplay
import com.sorbo.app.ui.theme.SorboTheme

/** Pantalla provisoria para ver todos los componentes. Se reemplaza cuando exista la navegación. */
@Composable
fun GaleriaComponentes(modifier: Modifier = Modifier) {
    var meGusto by remember { mutableStateOf<Boolean?>(null) }
    var interruptor by remember { mutableStateOf(true) }
    var chip by remember { mutableStateOf(true) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Sorbo", style = EstiloMarcaDisplay, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))
            Seccion("Botones") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}) { Text("Guardar") }
                    FilledTonalButton(onClick = {}) { Text("Tonal") }
                    OutlinedButton(onClick = {}) { Text("Contorno") }
                    TextButton(onClick = {}) { Text("Texto") }
                }
            }
            Seccion("Chips y veredicto") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = chip, onClick = { chip = !chip }, label = { Text("Carne") })
                    FilterChip(selected = !chip, onClick = { chip = !chip }, label = { Text("Pasta") })
                    Veredicto(meGusto = true)
                    Veredicto(meGusto = false)
                }
            }
            Seccion("¿Te gustó?") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OpcionGrande(true, meGusto == true, { meGusto = true }, Modifier.weight(1f))
                    OpcionGrande(false, meGusto == false, { meGusto = false }, Modifier.weight(1f))
                }
            }
            Seccion("Avisos") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Banner(TipoBanner.SinConexion, "Tu diario funciona igual.", titulo = "Sin conexión.")
                    Banner(TipoBanner.IA, "Revisalos antes de guardar.", titulo = "Completamos estos datos leyendo la etiqueta.")
                    Banner(TipoBanner.Error, "Puede ser por la luz o el ángulo.", titulo = "No pudimos leer la etiqueta.", textoAccion = "Reintentar")
                }
            }
            Column {
                Text("Diario", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                ItemVino("Rutini Malbec 2021", "Rutini Wines · Malbec", "2 veces · hace 3 días", true, "R", TonoEtiqueta.Malbec, {})
                ItemVino("El Esteco Torrontés", "El Esteco · Torrontés", "1 vez · hace 2 semanas", true, "EE", TonoEtiqueta.Dorado, {})
                ItemVino("Zuccardi Q", "Sin bodega", "1 vez · hoy", false, "ZQ", TonoEtiqueta.Verde, {}, pendienteRespaldo = true)
            }
            Seccion("Miniaturas") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TonoEtiqueta.entries.forEach { MiniaturaEtiqueta(it.name.take(2), it, tamano = 44.dp) }
                }
            }
            Seccion("Degustación") {
                TarjetaDegustacion("14 de septiembre", true, "Carne", "Amigos", "Asado", dulzor = "Seco", nota = "Ideal para el asado del domingo")
            }
            Column {
                FilaEstadistica("Secos", "8 de 9", 8f / 9f)
                FilaEstadistica("Dulces", "1 de 4", 0.25f, positiva = false)
            }
            Column {
                ItemAjustes("Resumen de gustos con IA", R.drawable.ic_ia, subtitulo = "Se envían tus estadísticas", final = FinalItemAjustes.Interruptor(interruptor) { interruptor = it })
                ItemAjustes("Cómo usamos tus datos", R.drawable.ic_info, final = FinalItemAjustes.Flecha, onClick = {})
            }
            EstadoVacio(
                icono = R.drawable.ic_vino,
                titulo = "Registrá tu primer vino",
                mensaje = "Sacale una foto a la etiqueta y anotá si te gustó.",
                textoAccion = "Registrar mi primer vino",
                modifier = Modifier.fillMaxWidth().height(320.dp),
            )
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        contenido()
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun GaleriaClaraPreview() = SorboTheme(oscuro = false) { GaleriaComponentes() }

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun GaleriaOscuraPreview() = SorboTheme(oscuro = true) { GaleriaComponentes() }
