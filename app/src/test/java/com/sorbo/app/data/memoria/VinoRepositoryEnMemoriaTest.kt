package com.sorbo.app.data.memoria

import com.sorbo.app.data.VinoRepositoryContratoTest
import com.sorbo.app.domain.repository.VinoRepository
import java.time.Clock

/** Corre la batería de contrato contra la versión en memoria. */
class VinoRepositoryEnMemoriaTest : VinoRepositoryContratoTest() {

    override fun crearRepositorio(reloj: Clock): VinoRepository = VinoRepositoryEnMemoria(reloj)
}
