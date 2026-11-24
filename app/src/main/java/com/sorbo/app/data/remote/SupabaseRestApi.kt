// Capa de datos · API de las tablas y de las fotos de Supabase con Retrofit (respaldo del diario).
package com.sorbo.app.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Las tablas se usan por la API REST estándar de Supabase (PostgREST). Guardar es un *upsert*:
 * `Prefer: resolution=merge-duplicates` hace que una fila con la misma `id` se reemplace en vez de duplicarse.
 * La seguridad por fila de la nube garantiza que cada usuario solo lea y escriba lo suyo.
 */
interface SupabaseRestApi {

    @Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("rest/v1/vinos")
    suspend fun guardarVinos(
        @Header("Authorization") autorizacion: String,
        @Body vinos: List<VinoDto>,
    ): Response<Unit>

    @Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("rest/v1/degustaciones")
    suspend fun guardarDegustaciones(
        @Header("Authorization") autorizacion: String,
        @Body degustaciones: List<DegustacionDto>,
    ): Response<Unit>

    /** Filtros de PostgREST: `usuario_id=eq.<id>`, `eliminado=eq.false`. */
    @GET("rest/v1/vinos")
    suspend fun buscarIdsDeVinos(
        @Header("Authorization") autorizacion: String,
        @Query("usuario_id") usuarioId: String,
        @Query("eliminado") eliminado: String = "eq.false",
        @Query("select") campos: String = "id",
        @Query("limit") limite: Int = 1,
    ): Response<List<IdDto>>

    /** Cuenta sin traer filas: el total llega en el encabezado `Content-Range` (`0-0/57`). */
    @Headers("Prefer: count=exact")
    @GET("rest/v1/vinos")
    suspend fun contarVinos(
        @Header("Authorization") autorizacion: String,
        @Query("usuario_id") usuarioId: String,
        @Query("select") campos: String = "id",
        @Query("limit") limite: Int = 1,
    ): Response<List<IdDto>>

    @GET("rest/v1/vinos")
    suspend fun bajarVinos(
        @Header("Authorization") autorizacion: String,
        @Query("usuario_id") usuarioId: String,
        @Query("offset") desde: Int,
        @Query("limit") cantidad: Int,
        @Query("order") orden: String = "id.asc",
    ): Response<List<VinoDto>>

    @GET("rest/v1/degustaciones")
    suspend fun bajarDegustaciones(
        @Header("Authorization") autorizacion: String,
        @Query("usuario_id") usuarioId: String,
        @Query("offset") desde: Int,
        @Query("limit") cantidad: Int,
        @Query("order") orden: String = "id.asc",
    ): Response<List<DegustacionDto>>

    @GET("storage/v1/object/etiquetas/{ruta}")
    suspend fun bajarFoto(
        @Header("Authorization") autorizacion: String,
        @Path(value = "ruta", encoded = true) ruta: String,
    ): Response<ResponseBody>

    /**
     * Sube la foto al bucket privado `etiquetas`. [ruta] es `{usuario_id}/{vino_id}.jpg` (va sin codificar
     * para que la barra quede como carpeta). `x-upsert` reemplaza la foto si ya existía.
     */
    @Headers("x-upsert: true")
    @POST("storage/v1/object/etiquetas/{ruta}")
    suspend fun subirFoto(
        @Header("Authorization") autorizacion: String,
        @Path(value = "ruta", encoded = true) ruta: String,
        @Body foto: RequestBody,
    ): Response<Unit>
}
