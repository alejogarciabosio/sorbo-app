// Capa de datos · respaldo simulado en memoria: hace de "nube" para probar la sincronización sin Supabase.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.repository.RespaldoRemoto

/**
 * Guarda lo subido en mapas por id (así un *upsert* nunca duplica). Con [falla] se simula un error
 * en la próxima operación, para probar qué hace la sincronización cuando la nube no responde.
 */
class RespaldoRemotoEnMemoria : RespaldoRemoto {

    val vinos = mutableMapOf<String, Vino>()
    val degustaciones = mutableMapOf<String, Degustacion>()

    /** Ruta en la nube → ruta local de la foto que se subió. */
    val fotos = mutableMapOf<String, String>()

    /** Si tiene valor, la próxima operación falla con esta excepción (y se limpia). */
    var falla: RespaldoException? = null

    /** Cuántas operaciones se pidieron: sirve para comprobar que no se sube de más. */
    var operaciones = 0
        private set

    override suspend fun subirFoto(usuarioId: String, vinoId: String, rutaLocal: String): String {
        registrar()
        val ruta = "$usuarioId/$vinoId.jpg"
        fotos[ruta] = rutaLocal
        return ruta
    }

    override suspend fun guardarVinos(vinos: List<Vino>) {
        registrar()
        vinos.forEach { this.vinos[it.id] = it }
    }

    override suspend fun guardarDegustaciones(degustaciones: List<Degustacion>) {
        registrar()
        degustaciones.forEach { this.degustaciones[it.id] = it }
    }

    override suspend fun hayRespaldo(usuarioId: String): Boolean {
        registrar()
        return vinos.values.any { it.usuarioId == usuarioId && !it.eliminado }
    }

    private fun registrar() {
        operaciones++
        falla?.let {
            falla = null
            throw it
        }
    }
}
