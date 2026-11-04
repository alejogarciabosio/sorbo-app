// Capa de presentación · navegación: rutas y mapa de pantallas (Navigation Compose, una sola Activity).
package com.sorbo.app.ui.navegacion

import java.net.URLEncoder

/** Todas las rutas de la app en un solo lugar, para no repetir textos sueltos. */
object Rutas {
    const val DIARIO = "diario"
    const val GUSTOS = "gustos"
    const val AJUSTES = "ajustes"

    const val BUSCAR = "buscar"
    const val ARG_NOMBRE = "nombre"
    const val REGISTRAR = "registrar?$ARG_NOMBRE={$ARG_NOMBRE}"

    const val ARG_VINO_ID = "vinoId"
    const val DETALLE = "detalle/{$ARG_VINO_ID}"
    const val EDITAR = "editar/{$ARG_VINO_ID}"
    const val VOLVER_A_PROBAR = "volver_a_probar/{$ARG_VINO_ID}"

    const val INICIAR_SESION = "iniciar_sesion"
    const val CREAR_CUENTA = "crear_cuenta"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val CUENTA = "cuenta"
    const val CAMBIAR_CONTRASENA = "cambiar_contrasena"
    const val PRIVACIDAD = "privacidad"

    const val GALERIA = "galeria"

    /** Registrar, con el nombre ya cargado si venimos de una búsqueda sin resultados. */
    fun registrar(nombre: String? = null): String {
        val nombreLimpio = nombre?.trim().orEmpty()
        if (nombreLimpio.isEmpty()) return "registrar"
        return "registrar?$ARG_NOMBRE=" + URLEncoder.encode(nombreLimpio, "UTF-8").replace("+", "%20")
    }

    fun detalle(vinoId: String) = "detalle/$vinoId"
    fun editar(vinoId: String) = "editar/$vinoId"
    fun volverAProbar(vinoId: String) = "volver_a_probar/$vinoId"

    /** Las tres pantallas de la barra inferior. */
    val principales = listOf(DIARIO, GUSTOS, AJUSTES)

    /** La barra inferior solo se muestra en las pantallas principales. */
    fun mostrarBarraInferior(ruta: String?) = ruta in principales
}
