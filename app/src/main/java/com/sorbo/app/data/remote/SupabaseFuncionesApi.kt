// Capa de datos · API de las Edge Functions de Supabase con Retrofit (las funciones de IA de Alejo).
package com.sorbo.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseFuncionesApi {

    /**
     * Resumen de gustos con IA. Errores: 401 `sin_sesion`, 400 `pedido_invalido`, 422 `pocas_degustaciones`,
     * 429 `limite` (5 por usuario y por día) y 502/503/504 `servicio`.
     */
    @POST("functions/v1/analizar-gustos")
    suspend fun analizarGustos(
        @Header("Authorization") autorizacion: String,
        @Body pedido: PedidoAnalisisDto,
    ): Response<RespuestaAnalisisDto>
}
