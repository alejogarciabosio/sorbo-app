// Capa de datos · proveedor de token: deja que otros clientes de Supabase hagan pedidos con el token del usuario.
package com.sorbo.app.data.remote

import retrofit2.Response

/**
 * Lo implementa `SesionRepositorySupabase`, que es quien guarda los tokens. Así el resumen con IA y la
 * sincronización usan la misma renovación automática del token, sin copiarla.
 */
interface ProveedorDeToken {

    /**
     * Ejecuta [llamada] con el token de acceso actual. Si Supabase dice que el token no sirve, lo renueva
     * una vez y reintenta. Sin sesión (o si la renovación es rechazada) lanza `CuentaException(SIN_SESION)`.
     */
    suspend fun <C> conToken(llamada: suspend (String) -> Response<C>): Response<C>
}
