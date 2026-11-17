// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.LecturaNube
import com.sorbo.app.domain.model.SugerenciaEtiqueta
import com.sorbo.app.domain.repository.LectorEtiquetaNube
import com.sorbo.app.domain.util.normalizar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.util.Locale

/**
 * Pide la lectura de la etiqueta al modelo de visión de la nube, si se puede, y devuelve solo lo que pasa los controles.
 * Devuelve null (y la app sigue con la lectura del teléfono, sin avisar de ningún error) si no hay permiso, si tarda más
 * de [esperaMs] o si falla.
 */
class LeerEtiquetaEnLaNube(
    private val puedeLeerEnLaNube: PuedeLeerEnLaNube,
    private val lector: LectorEtiquetaNube,
    private val esperaMs: Long = ESPERA_MS,
    private val anioMaximo: () -> Int = { LocalDate.now().year + 1 },
) {
    suspend operator fun invoke(fotoPath: String): SugerenciaEtiqueta? {
        if (!puedeLeerEnLaNube()) return null
        val lectura = try {
            withTimeoutOrNull(esperaMs) { lector.leer(fotoPath).getOrNull() }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (falla: Exception) {
            null
        } ?: return null
        return validarLecturaNube(lectura, anioMaximo()).takeUnless { it.estaVacia }
    }

    companion object {
        const val ESPERA_MS = 5_000L
    }
}

/**
 * El modelo a veces responde datos que parecen razonables pero no están en la etiqueta. Cada dato se acepta solo si lo que
 * dice (palabra por palabra) figura en los renglones que el propio modelo dice haber leído; el año además tiene que ser
 * plausible. Sin renglones no hay nada que comprobar y se descarta todo.
 */
fun validarLecturaNube(lectura: LecturaNube, anioMaximo: Int): SugerenciaEtiqueta {
    val palabras = lectura.lineas.flatMap { normalizar(it).split(' ') }.filter { it.isNotBlank() }.toSet()
    if (palabras.isEmpty()) return SugerenciaEtiqueta()

    fun textoVerificado(valor: String?): String? {
        val limpio = valor?.trim()?.takeIf { it.length in 2..LARGO_MAXIMO } ?: return null
        // Los conectores ("Malbec y Syrah") los pone el modelo para unir cepas y no figuran como renglón en la etiqueta.
        val partes = normalizar(limpio).split(' ').filter { it.isNotBlank() && it !in CONECTORES }
        return limpio.takeIf { partes.isNotEmpty() && partes.all { it in palabras } }
    }

    return SugerenciaEtiqueta(
        nombre = textoVerificado(lectura.nombre),
        bodega = textoVerificado(lectura.bodega),
        cepa = textoVerificado(lectura.cepa)?.let(::cepaConFormato),
        anio = lectura.anio?.takeIf { it in ANIO_MINIMO..anioMaximo && it.toString() in palabras },
    )
}

private const val LARGO_MAXIMO = 80
private val CONECTORES = setOf("y", "e")
private const val ANIO_MINIMO = 1900

/** Las etiquetas escriben la cepa en mayúsculas ("MALBEC"); en el formulario se muestra como "Malbec" ("Malbec y Syrah"). */
private fun cepaConFormato(cepa: String): String =
    cepa.split(' ').joinToString(" ") { palabra ->
        if (palabra.length > 1 && palabra == palabra.uppercase(Locale.ROOT)) {
            palabra.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
        } else {
            palabra
        }
    }
