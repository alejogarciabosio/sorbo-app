// Capa de datos · cliente HTTP de Supabase: arma OkHttp y Retrofit una sola vez, con la clave y los tiempos de espera.
package com.sorbo.app.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * La URL y la clave anónima llegan desde `BuildConfig` (que las lee de local.properties): nunca están en el código.
 * No se registra ningún pedido en el log, para no dejar tokens ni contraseñas a la vista.
 */
object ClienteSupabase {

    private const val SEGUNDOS_DE_ESPERA = 10L

    // La IA puede tardar más: la función prueba otro modelo si el primero no responde.
    private const val SEGUNDOS_DE_ESPERA_IA = 30L

    fun crearAuthApi(urlBase: String, claveAnonima: String): SupabaseAuthApi =
        crearRetrofit(urlBase, claveAnonima).create(SupabaseAuthApi::class.java)

    /**
     * Para las tablas el JSON incluye los campos vacíos (`"bodega": null`): si se omitieran, el *upsert* dejaría
     * el valor viejo en la nube, y PostgREST exige que todas las filas de un lote tengan las mismas claves.
     */
    fun crearRestApi(urlBase: String, claveAnonima: String): SupabaseRestApi =
        crearRetrofit(urlBase, claveAnonima, gson = GsonBuilder().serializeNulls().create()).create(SupabaseRestApi::class.java)

    fun crearFuncionesApi(urlBase: String, claveAnonima: String): SupabaseFuncionesApi =
        crearRetrofit(urlBase, claveAnonima, SEGUNDOS_DE_ESPERA_IA).create(SupabaseFuncionesApi::class.java)

    private fun crearRetrofit(
        urlBase: String,
        claveAnonima: String,
        segundosDeLectura: Long = SEGUNDOS_DE_ESPERA,
        gson: Gson = Gson(),
    ): Retrofit {
        val http = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .readTimeout(segundosDeLectura, TimeUnit.SECONDS)
            .writeTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            // Supabase exige la clave anónima en todos los pedidos.
            .addInterceptor { cadena ->
                cadena.proceed(cadena.request().newBuilder().header("apikey", claveAnonima).build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(urlBase.trimEnd('/') + "/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
