// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

/** La persona que inició sesión. El [id] es el mismo de Supabase Auth. */
data class Usuario(
    val id: String,
    val email: String,
)

/** La sesión es opcional: la app se usa completa sin cuenta. */
sealed interface EstadoSesion {
    data object SinSesion : EstadoSesion
    data class ConSesion(val usuario: Usuario) : EstadoSesion
}
