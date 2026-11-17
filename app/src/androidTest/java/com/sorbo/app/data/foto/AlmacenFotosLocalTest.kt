package com.sorbo.app.data.foto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Corre en un teléfono o emulador: usa la carpeta real de la app (`filesDir/fotos`). */
@RunWith(AndroidJUnit4::class)
class AlmacenFotosLocalTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val carpeta = File(context.filesDir, "fotos")
    private val almacen = AlmacenFotosLocal(context)

    @Before
    @After
    fun vaciar() {
        carpeta.deleteRecursively()
    }

    @Test
    fun listarDevuelveLasFotosGuardadas() {
        carpeta.mkdirs()
        val a = File(carpeta, "a.jpg").apply { writeText("a") }
        val b = File(carpeta, "b.jpg").apply { writeText("b") }

        assertEquals(setOf(a.absolutePath, b.absolutePath), almacen.listar().toSet())
    }

    @Test
    fun sinFotosListarDevuelveVacio() {
        assertTrue(almacen.listar().isEmpty())
    }

    @Test
    fun loBorradoYaNoSeLista() {
        carpeta.mkdirs()
        val a = File(carpeta, "a.jpg").apply { writeText("a") }

        almacen.borrar(a.absolutePath)

        assertTrue(almacen.listar().isEmpty())
    }
}
