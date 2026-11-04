// Capa de presentación · componente reutilizable sin estado (state hoisting): las preguntas sobre una degustación, que usan Registrar y "Lo volví a probar".
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import java.time.LocalDate

/** Lo que el usuario fue contestando sobre una degustación. */
data class RespuestasDeDegustacion(
    val meGusto: Boolean?,
    val dulzor: Dulzor?,
    val comida: Comida?,
    val compania: Compania?,
    val ocasion: Ocasion?,
    val fecha: LocalDate,
    val nota: String,
)

/** Qué hacer con cada respuesta. Se pasa aparte para que el estado lo guarde quien usa el componente. */
class AccionesDeDegustacion(
    val onMeGusto: (Boolean) -> Unit,
    val onDulzor: (Dulzor) -> Unit,
    val onComida: (Comida) -> Unit,
    val onCompania: (Compania) -> Unit,
    val onOcasion: (Ocasion) -> Unit,
    val onNota: (String) -> Unit,
    val onElegirFecha: () -> Unit,
)

/**
 * Las preguntas sobre una degustación: ¿te gustó? (obligatoria), cómo lo sentiste, con qué lo comiste, con quién,
 * en qué ocasión, cuándo y una nota. [detalleDelDulzor] es un texto de ayuda opcional debajo de "¿Cómo lo sentiste?".
 */
@Composable
fun PreguntasDeDegustacion(
    respuestas: RespuestasDeDegustacion,
    hoy: LocalDate,
    faltaOpinion: Boolean,
    acciones: AccionesDeDegustacion,
    modifier: Modifier = Modifier,
    detalleDelDulzor: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Seccion(R.string.registro_te_gusto) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpcionGrande(
                    meGusto = true,
                    seleccionada = respuestas.meGusto == true,
                    onClick = { acciones.onMeGusto(true) },
                    modifier = Modifier.weight(1f),
                    conError = faltaOpinion,
                )
                OpcionGrande(
                    meGusto = false,
                    seleccionada = respuestas.meGusto == false,
                    onClick = { acciones.onMeGusto(false) },
                    modifier = Modifier.weight(1f),
                    conError = faltaOpinion,
                )
            }
            if (faltaOpinion) {
                Text(
                    stringResource(R.string.registro_falta_opinion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Seccion(R.string.registro_como_lo_sentiste) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Dulzor.entries.forEachIndexed { posicion, dulzor ->
                    SegmentedButton(
                        selected = respuestas.dulzor == dulzor,
                        onClick = { acciones.onDulzor(dulzor) },
                        shape = SegmentedButtonDefaults.itemShape(posicion, Dulzor.entries.size),
                    ) {
                        Text(stringResource(dulzor.etiqueta()))
                    }
                }
            }
            if (detalleDelDulzor != null) {
                Text(detalleDelDulzor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Seccion(R.string.registro_con_que_lo_comiste) {
            ChipsDeOpciones(Comida.entries, respuestas.comida, Comida::etiqueta, acciones.onComida)
        }
        Seccion(R.string.registro_con_quien) {
            ChipsDeOpciones(Compania.entries, respuestas.compania, Compania::etiqueta, acciones.onCompania)
        }
        Seccion(R.string.registro_ocasion) {
            ChipsDeOpciones(Ocasion.entries, respuestas.ocasion, Ocasion::etiqueta, acciones.onOcasion)
        }

        FilaFecha(fecha = respuestas.fecha, hoy = hoy, onCambiarFecha = acciones.onElegirFecha)

        OutlinedTextField(
            value = respuestas.nota,
            onValueChange = acciones.onNota,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_nota)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

/** Una pregunta del formulario con su título y las opciones debajo. */
@Composable
private fun Seccion(titulo: Int, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium)
        contenido()
    }
}
