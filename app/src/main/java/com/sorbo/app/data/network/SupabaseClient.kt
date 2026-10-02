package com.sorbo.app.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.Auth
import com.sorbo.app.BuildConfig

object SupabaseClient {
    // Inicializamos el cliente de Supabase usando las variables inyectadas desde local.properties por BuildConfig
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth)
    }
}
