// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Surface
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** El día en que se probó el vino: muestra la fecha y, al tocarla, abre el selector. */
@Composable
fun FilaFecha(fecha: LocalDate, hoy: LocalDate, onCambiarFecha: () -> Unit, modifier: Modifier = Modifier) {
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    val textoFecha = fecha.format(formato)
    val valor = if (fecha == hoy) stringResource(R.string.registro_fecha_hoy, textoFecha) else textoFecha
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.registro_fecha_cambiar), onClick = onCambiarFecha)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(painterResource(R.drawable.ic_fecha), contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(stringResource(R.string.registro_fecha_titulo), style = MaterialTheme.typography.titleMedium)
                Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Selector de fecha de Material 3: no deja elegir días posteriores a [hoy].
 * El calendario de Material mide 360 dp fijos: en pantallas más angostas (hasta 320 dp, RNF11) se le recortaría la columna del
 * domingo, así que ahí se usa la entrada por teclado (dd/mm/aaaa), que sí se adapta al ancho disponible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDeFecha(fecha: LocalDate, hoy: LocalDate, onElegir: (LocalDate) -> Unit, onCerrar: () -> Unit) {
    val anchoDePantalla = LocalConfiguration.current.screenWidthDp
    val alcanzaParaElCalendario = anchoDePantalla >= ANCHO_MINIMO_DEL_CALENDARIO_DP
    // Con poco margen sobrante el diálogo ocupa todo el ancho: el calendario no entra con los 8 dp de cada lado.
    val margenLateral = if (anchoDePantalla >= ANCHO_MINIMO_DEL_CALENDARIO_DP + 16) 8.dp else 0.dp
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fecha.aMillisUtc(),
        initialDisplayMode = if (alcanzaParaElCalendario) DisplayMode.Picker else DisplayMode.Input,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoy.aMillisUtc()
            override fun isSelectableYear(year: Int) = year <= hoy.year
        },
    )
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = margenLateral).widthIn(max = 400.dp),
            shape = DatePickerDefaults.shape,
            color = DatePickerDefaults.colors().containerColor,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DatePicker(state = estado, modifier = Modifier.fillMaxWidth(), showModeToggle = alcanzaParaElCalendario)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onCerrar) { Text(stringResource(R.string.accion_cancelar)) }
                    TextButton(
                        onClick = {
                            estado.selectedDateMillis?.let { onElegir(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                            onCerrar()
                        },
                    ) { Text(stringResource(R.string.accion_aceptar)) }
                }
            }
        }
    }
}

private const val ANCHO_MINIMO_DEL_CALENDARIO_DP = 360

// El selector de Material trabaja con medianoche UTC del día elegido.
private fun LocalDate.aMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
