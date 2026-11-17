// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

/** Guarda las fotos de las etiquetas en el teléfono, ya corregidas y de a lo sumo 500 KB. */
interface AlmacenFotos {

    /**
     * Copia la foto que sacó o eligió el usuario ([origen] es su dirección) al almacenamiento de la app.
     * Devuelve la ruta de la copia, o el error si no se pudo leer o achicar.
     */
    suspend fun guardar(origen: String): Result<String>

    /** Borra una foto guardada por [guardar]. Si no existe, no hace nada. */
    fun borrar(ruta: String)

    /**
     * Las rutas de todas las fotos guardadas. Sirve para encontrar las que ningún vino usa
     * (por ejemplo, si Android cerró la app a mitad de un registro) y borrarlas.
     */
    fun listar(): List<String>
}
