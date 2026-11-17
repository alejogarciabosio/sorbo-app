package com.sorbo.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sorbo.app.data.VinoRepositoryContratoTest
import com.sorbo.app.domain.repository.VinoRepository
import org.junit.After
import org.junit.runner.RunWith
import java.time.Clock

/**
 * Corre la batería de contrato contra Room, en un teléfono o emulador, con una base en memoria
 * (se crea vacía para cada test y no toca los datos reales de la app).
 */
@RunWith(AndroidJUnit4::class)
class VinoRepositoryRoomTest : VinoRepositoryContratoTest() {

    private lateinit var base: SorboDatabase

    override fun crearRepositorio(reloj: Clock): VinoRepository {
        base = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SorboDatabase::class.java).build()
        return VinoRepositoryRoom(base.vinoDao(), reloj)
    }

    @After
    fun cerrarBase() {
        base.close()
    }
}
