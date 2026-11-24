// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.repository.VinoRepository

/**
 * Borra las fotos guardadas que ningún vino del diario usa: por ejemplo, si Android cerró la app a mitad
 * de un registro, o las de vinos ya eliminados. Se llama al abrir la app **en frío**, antes de mostrar la
 * interfaz, porque en ese momento no hay ningún formulario abierto que pueda estar usando una foto.
 * Si la app se está restaurando (Android la cerró con un formulario abierto) no se llama: esa foto sigue en uso.
 */
class LimpiarFotosHuerfanas(
    private val vinos: VinoRepository,
    private val fotos: AlmacenFotos,
) {

    /** Devuelve cuántas fotos borró. */
    suspend operator fun invoke(): Int {
        // Primero la lista de fotos: una foto que se guarde después de este momento nunca entra en la limpieza.
        val guardadas = fotos.listar()
        val enUso = vinos.listarVinos().mapNotNull { it.fotoPath }.toSet()
        val huerfanas = guardadas.filter { it !in enUso }
        huerfanas.forEach(fotos::borrar)
        return huerfanas.size
    }
}
