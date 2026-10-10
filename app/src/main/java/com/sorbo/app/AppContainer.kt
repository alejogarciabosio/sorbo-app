package com.sorbo.app

import android.content.Context

/**
 * Inyección de dependencias manual: crea una sola vez las dependencias compartidas
 * (base de datos, repositorios, cliente de Supabase) y se las pasa a los ViewModels.
 */
class AppContainer(private val context: Context)
