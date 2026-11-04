package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.TextoEtiqueta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Las reglas contra lo que ML Kit leyó de verdad en siete botellas (ver [EtiquetasReales]). */
class InterpretarEtiquetasRealesTest {

    private val interpretar = InterpretarTextoEtiqueta()

    private fun leer(etiqueta: TextoEtiqueta) = interpretar(etiqueta)

    @Test
    fun leeLaCepaAunqueElOcrConfundaUnaLetra() {
        // ML Kit leyó "MALEEC" (la B como E) en Desierto Pampa; aparece recién al girar la foto.
        assertEquals("Malbec", leer(EtiquetasReales.desiertoPampa).cepa)
        // ... y "MALBEG" (la C como G) en Kaiken, que sí está en la lectura derecha.
        assertEquals("Malbec", leer(EtiquetasReales.kaikenUltra).cepa)
    }

    @Test
    fun completaLaCepaGirandoLaFotoCuandoLaBotellaEstabaAcostada() {
        assertEquals("Cabernet Franc", leer(EtiquetasReales.sombrero).cepa)
    }

    @Test
    fun laCepaSeLeeBienEnLasEtiquetasQueYaLaTenian() {
        assertEquals("Malbec", leer(EtiquetasReales.joseZuccardi).cepa)
        assertEquals("Malbec", leer(EtiquetasReales.chanarmuyo).cepa)
        assertEquals("Cabernet Sauvignon", leer(EtiquetasReales.antesYDespues).cepa)
    }

    @Test
    fun siLaEtiquetaNoTraeCepaNoInventaNinguna() {
        assertNull(leer(EtiquetasReales.lucaBesoDeDante).cepa)
    }

    @Test
    fun leeElAnio() {
        assertEquals(2023, leer(EtiquetasReales.kaikenUltra).anio)
        assertEquals(2017, leer(EtiquetasReales.joseZuccardi).anio)
        assertEquals(2021, leer(EtiquetasReales.lucaBesoDeDante).anio)
        assertEquals(2023, leer(EtiquetasReales.chanarmuyo).anio)
        assertEquals(2025, leer(EtiquetasReales.sombrero).anio)
        assertEquals(2023, leer(EtiquetasReales.antesYDespues).anio)
    }

    @Test
    fun elAnioQueElOcrNoLlegoAVerQuedaVacioEnLugarDeInventarse() {
        // El 2022 de Desierto Pampa es rojo, chico y sobre gris: ML Kit no lo lee en ninguna orientación.
        assertNull(leer(EtiquetasReales.desiertoPampa).anio)
    }

    @Test
    fun elNombreNoEsLaCepaNiLaZona() {
        val nombre = leer(EtiquetasReales.sombrero).nombre.orEmpty().lowercase()
        assertTrue("nombre: $nombre", "cabernet" !in nombre && "uco" !in nombre)
        val kaiken = leer(EtiquetasReales.kaikenUltra).nombre.orEmpty().lowercase()
        assertTrue("nombre: $kaiken", "kaiken" in kaiken && "argentina" !in kaiken && "mendo" !in kaiken)
    }

    @Test
    fun elNombreSeArmaConLosRenglonesGrandesContiguos() {
        // El OCR leyó dos veces "DESIERTO" ("DESIERTO" y "DEST ERTO"): sale una sola.
        assertEquals("DESIERTO PAMPA", leer(EtiquetasReales.desiertoPampa).nombre)
        assertEquals("SOM BRE RO", leer(EtiquetasReales.sombrero).nombre)
    }

    @Test
    fun elNombreSaleDeLaEtiquetaYNoDelTextoDelFondo() {
        // En la foto de Zuccardi hay botellas envueltas con la marca repetida detrás: el texto más grande es basura.
        assertEquals("JOSÉ ZUcCARDI", leer(EtiquetasReales.joseZuccardi).nombre)
    }

    @Test
    fun cuandoEsLegibleElNombreQuedaIntacto() {
        assertTrue(leer(EtiquetasReales.lucaBesoDeDante).nombre.orEmpty().uppercase().startsWith("LUC"))
        assertTrue(leer(EtiquetasReales.chanarmuyo).nombre.orEmpty().uppercase().startsWith("CHANARMUY"))
    }

    @Test
    fun laBodegaSeLeeDeLaLecturaGiradaSiLaDerechaNoLaTrae() {
        assertNotNull(leer(EtiquetasReales.sombrero).bodega)
        assertTrue(leer(EtiquetasReales.sombrero).bodega.orEmpty().uppercase().endsWith("WINES"))
    }

    @Test
    fun conSieteBotellasRealesAcierta_AnioYCepaEnAlMenosElSetentaPorCiento() {
        // Criterio de la preentrega: año y cepa correctos en al menos 7 de 10 etiquetas.
        val esperadas = listOf(
            Triple(EtiquetasReales.desiertoPampa, 2022, "Malbec"),
            Triple(EtiquetasReales.kaikenUltra, 2023, "Malbec"),
            Triple(EtiquetasReales.joseZuccardi, 2017, "Malbec"),
            Triple(EtiquetasReales.lucaBesoDeDante, 2021, null),
            Triple(EtiquetasReales.chanarmuyo, 2023, "Malbec"),
            Triple(EtiquetasReales.sombrero, 2025, "Cabernet Franc"),
            Triple(EtiquetasReales.antesYDespues, 2023, "Cabernet Sauvignon"),
        )
        val bien = esperadas.count { (etiqueta, anio, cepa) -> leer(etiqueta).let { it.anio == anio && it.cepa == cepa } }
        assertTrue("año y cepa bien en $bien de ${esperadas.size}", bien.toFloat() / esperadas.size >= 0.7f)
    }
}
