// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.extraerAnio
import kotlinx.coroutines.flow.first

/** Los datos del vino que se pueden corregir. Las degustaciones se cambian desde su propia ficha, no acá. */
data class DatosDelVino(
    val nombre: String,
    val bodega: String? = null,
    val cepa: String? = null,
    val anio: Int? = null,
    val fotoPath: String? = null,
)

sealed interface ResultadoEdicion {
    /** El nombre es obligatorio, igual que al registrar. */
    data object NombreVacio : ResultadoEdicion

    /** El vino ya no existe (se eliminó mientras se editaba). */
    data object NoExiste : ResultadoEdicion

    /** [fotoAnterior] es la foto que tenía el vino y quedó reemplazada, para que quien llama la pueda borrar. */
    data class Guardado(val fotoAnterior: String?) : ResultadoEdicion
}

/** Valida los datos corregidos y los guarda sobre el mismo vino (no crea uno nuevo ni toca sus degustaciones). */
class EditarVino(private val repositorio: VinoRepository) {

    suspend operator fun invoke(vinoId: String, datos: DatosDelVino): ResultadoEdicion {
        val nombre = datos.nombre.trim()
        if (nombre.isEmpty()) return ResultadoEdicion.NombreVacio
        val original = repositorio.observarVino(vinoId).first()?.vino ?: return ResultadoEdicion.NoExiste

        repositorio.actualizarVino(
            original.copy(
                nombre = nombre,
                bodega = datos.bodega.limpio(),
                cepa = datos.cepa.limpio(),
                anio = datos.anio ?: extraerAnio(nombre),
                fotoPath = datos.fotoPath,
            ),
        )
        return ResultadoEdicion.Guardado(fotoAnterior = original.fotoPath.takeIf { it != datos.fotoPath })
    }

    private fun String?.limpio(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
