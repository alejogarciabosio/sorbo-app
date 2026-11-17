// Capa de datos · cliente HTTP de Supabase: arma OkHttp y Retrofit una sola vez, con la clave y los tiempos de espera.
package com.sorbo.app.data.remote

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

    fun crearAuthApi(urlBase: String, claveAnonima: String): SupabaseAuthApi =
        crearRetrofit(urlBase, claveAnonima).create(SupabaseAuthApi::class.java)

    private fun crearRetrofit(urlBase: String, claveAnonima: String): Retrofit {
        val http = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .writeTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            // Supabase exige la clave anónima en todos los pedidos.
            .addInterceptor { cadena ->
                cadena.proceed(cadena.request().newBuilder().header("apikey", claveAnonima).build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(urlBase.trimEnd('/') + "/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
