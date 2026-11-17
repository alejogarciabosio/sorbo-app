// Capa de datos · API de Supabase Auth con Retrofit: cada función es un pedido HTTP de la tabla del documento 03.
package com.sorbo.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Retrofit genera la implementación. Se devuelve [Response] (y no el cuerpo directo) para poder leer
 * el código HTTP y el cuerpo de error y traducirlos a `MotivoCuenta`.
 * El encabezado `apikey` lo agrega el cliente a todos los pedidos; el `Authorization` va solo en los del usuario.
 */
interface SupabaseAuthApi {

    @POST("auth/v1/signup")
    suspend fun crearCuenta(@Body credenciales: CredencialesDto): Response<SesionDto>

    @POST("auth/v1/token?grant_type=password")
    suspend fun iniciarSesion(@Body credenciales: CredencialesDto): Response<SesionDto>

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun renovarSesion(@Body cuerpo: RenovarSesionDto): Response<SesionDto>

    /** [volverA] es el deep link que abre la app desde el email (decisión abierta #4: `sorbo://recuperar`). */
    @POST("auth/v1/recover")
    suspend fun recuperarContrasena(
        @Body cuerpo: EmailDto,
        @Query("redirect_to") volverA: String,
    ): Response<Unit>

    @PUT("auth/v1/user")
    suspend fun cambiarContrasena(
        @Header("Authorization") autorizacion: String,
        @Body cuerpo: ClaveNuevaDto,
    ): Response<UsuarioDto>

    @POST("auth/v1/logout")
    suspend fun cerrarSesion(@Header("Authorization") autorizacion: String): Response<Unit>
}
