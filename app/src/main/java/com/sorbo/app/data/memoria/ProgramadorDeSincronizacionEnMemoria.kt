// Capa de datos · programador simulado: anota cuántas veces se pidió sincronizar, hasta que exista el de WorkManager (I5).
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.repository.ProgramadorDeSincronizacion

class ProgramadorDeSincronizacionEnMemoria : ProgramadorDeSincronizacion {

    var veces = 0
        private set

    override fun programar() {
        veces++
    }
}
