// Capa de presentación · componente reutilizable sin estado (state hoisting): el bloque del resumen con IA de Mis gustos.
package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.ui.viewmodels.EstadoResumenIa
import java.time.LocalDate
import java.time.ZoneId

/**
 * Lo que Mis gustos muestra de la IA según [estado]: pedir el resumen, esperarlo, verlo (también sin conexión) o entender por qué no salió.
 * Todo lo que escribe la IA lleva el ✦ y el color tertiary, como en el resto de la app.
 */
@Composable
fun ResumenConIa(
    estado: EstadoResumenIa,
    hoy: LocalDate,
    alGenerar: () -> Unit,
    alAceptarAviso: () -> Unit,
    alRechazarAviso: () -> Unit,
    alDescartarError: () -> Unit,
    onIniciarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (estado) {
        EstadoResumenIa.Oculto -> Unit
        EstadoResumenIa.RequiereCuenta -> RequiereCuenta(onIniciarSesion, modifier)
        is EstadoResumenIa.Disponible -> {
            if (estado.pideAviso) AvisoDeIa(alAceptarAviso, alRechazarAviso)
            Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                estado.ultimo?.let { ultimo ->
                    ResumenGuardado(ultimo, hoy, estado.nuevasDesdeElUltimo)
                }
                if (!estado.hayConexion) {
                    Banner(tipo = TipoBanner.SinConexion, mensaje = stringResource(R.string.ia_necesita_conexion))
                }
                BotonDeGenerar(conResumen = estado.ultimo != null, habilitado = estado.hayConexion, onClick = alGenerar)
            }
        }
        is EstadoResumenIa.Generando -> Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Generando()
            estado.ultimo?.let { ResumenGuardado(it, hoy, nuevas = 0) }
        }
        is EstadoResumenIa.ConError -> Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Banner(
                tipo = TipoBanner.Error,
                titulo = stringResource(R.string.ia_error_titulo),
                mensaje = stringResource(estado.motivo.mensaje()),
                textoAccion = stringResource(R.string.ia_reintentar),
                onAccion = alGenerar,
            )
            TextButton(onClick = alDescartarError, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.ia_cerrar_error))
            }
            estado.ultimo?.let { ResumenGuardado(it, hoy, nuevas = 0) }
        }
    }
}

/** El texto de cada motivo. El `when` es exhaustivo: un motivo nuevo obliga a escribir su mensaje. */
private fun MotivoAnalisis.mensaje(): Int = when (this) {
    MotivoAnalisis.SIN_CONEXION -> R.string.ia_error_sin_conexion
    MotivoAnalisis.SIN_SESION -> R.string.ia_error_sin_sesion
    MotivoAnalisis.LIMITE_ALCANZADO -> R.string.ia_error_limite
    MotivoAnalisis.SERVICIO_NO_DISPONIBLE -> R.string.ia_error_servicio
    MotivoAnalisis.ERROR_DESCONOCIDO -> R.string.ia_error_desconocido
}

@Composable
private fun RequiereCuenta(onIniciarSesion: () -> Unit, modifier: Modifier) {
    OutlinedCard(modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.ia_requiere_cuenta_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.ia_requiere_cuenta_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onIniciarSesion) { Text(stringResource(R.string.cuenta_iniciar)) }
        }
    }
}

@Composable
private fun BotonDeGenerar(conResumen: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = habilitado, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Icon(painterResource(R.drawable.ic_ia), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(if (conResumen) R.string.ia_actualizar else R.string.ia_generar))
    }
}

@Composable
private fun Generando() {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Column {
                Text(stringResource(R.string.ia_generando_titulo), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.ia_generando_mensaje),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** El último resumen: la tarjeta con su fecha, cuántas degustaciones se sumaron después y las sugerencias. */
@Composable
private fun ResumenGuardado(analisis: AnalisisGustos, hoy: LocalDate, nuevas: Int) {
    val colores = MaterialTheme.colorScheme
    val fecha = analisis.generadoEn.atZone(ZoneId.systemDefault()).toLocalDate()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(colores.primary, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_ia), contentDescription = null, modifier = Modifier.size(18.dp), tint = colores.onPrimary)
                Text(
                    stringResource(R.string.ia_tu_estilo).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.onPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Text(analisis.resumen, style = MaterialTheme.typography.bodyLarge, color = colores.onPrimary)
        }
        Text(
            stringResource(R.string.ia_generado_el, textoDeFecha(fecha, hoy)),
            style = MaterialTheme.typography.bodySmall,
            color = colores.onSurfaceVariant,
        )
        if (nuevas > 0) {
            Text(
                pluralStringResource(R.plurals.ia_nuevas_degustaciones, nuevas, nuevas),
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant,
            )
        }
        Text(
            stringResource(R.string.ia_sugerencias_titulo),
            style = MaterialTheme.typography.titleSmall,
            color = colores.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        analisis.sugerencias.forEach { sugerencia ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    IconoDeSugerencia(fondo = colores.tertiaryContainer)
                    Text(sugerencia, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                }
            }
        }
        Text(
            stringResource(R.string.ia_nota),
            style = MaterialTheme.typography.bodySmall,
            color = colores.onSurfaceVariant,
        )
    }
}

/** El círculo con el ✦ que marca lo que sugirió la IA. Es decorativo: la sugerencia se lee sola. */
@Composable
private fun IconoDeSugerencia(fondo: Color) {
    Box(
        modifier = Modifier.size(32.dp).background(fondo, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_ia),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

/** E7: el consentimiento de la primera vez. Dice qué se envía y qué no, y que se puede apagar en Ajustes. */
@Composable
fun AvisoDeIa(alAceptar: () -> Unit, alRechazar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alRechazar,
        icon = { Icon(painterResource(R.drawable.ic_ia), contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
        title = { Text(stringResource(R.string.ia_aviso_titulo)) },
        text = { Text(stringResource(R.string.ia_aviso_mensaje)) },
        confirmButton = { TextButton(onClick = alAceptar) { Text(stringResource(R.string.ia_aviso_usar)) } },
        dismissButton = { TextButton(onClick = alRechazar) { Text(stringResource(R.string.ia_aviso_ahora_no)) } },
    )
}
