package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta

/**
 * Texto crudo que devolvió ML Kit en el teléfono para fotos reales de botellas (sacadas con el celular, varias con la botella acostada).
 * La lectura principal es la foto derecha, como la recibe la app; las alternativas son la misma foto girada 90° a un lado y al otro.
 */
object EtiquetasReales {
    val desiertoPampa = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("a DESIERTO", 46f),
            BloqueTexto("DEST ERTO", 47f),
            BloqueTexto("PAMPA", 52f),
            BloqueTexto("EC", 20f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("EC", 37f),
                BloqueTexto("PAMPA", 201f),
                BloqueTexto("DESTERTO", 267f),
                BloqueTexto("a DESIERTO", 101f),
            ),
            listOf(
                BloqueTexto("HE DESIERTO", 103f),
                BloqueTexto("DESTERTO", 268f),
                BloqueTexto("PAMPA", 210f),
                BloqueTexto("MALEEC", 124f),
                BloqueTexto("rINA", 41f),
            ),
            ),
    )

    val kaikenUltra = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("KAIKEN", 325f),
            BloqueTexto("ulta", 197f),
            BloqueTexto("MALBEG", 134f),
            BloqueTexto("2023", 67f),
            BloqueTexto("MENDO ZA ARGENTINA", 253f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("MALBEG", 26f),
                BloqueTexto("KAIKEN", 63f),
                BloqueTexto("2023", 24f),
                BloqueTexto("dos", 25f),
                BloqueTexto("MENDOZA ARG ENTINA", 26f),
                BloqueTexto("2029", 12f),
            ),
            listOf(
                BloqueTexto("MENDO ZA ARGENTINA", 27f),
                BloqueTexto("2023", 29f),
                BloqueTexto("MALBEG", 32f),
                BloqueTexto("ulta", 119f),
                BloqueTexto("KAIKEN", 57f),
            ),
            ),
    )

    val joseZuccardi = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("JOSÉ", 39f),
            BloqueTexto("ZUcCARDI", 58f),
            BloqueTexto("MALBEC", 21f),
            BloqueTexto("VALLE DEUCOARGENTINA", 24f),
            BloqueTexto("2017", 12f),
            BloqueTexto("acARD,", 28f),
            BloqueTexto("e we o", 9f),
            BloqueTexto("ARDI", 21f),
            BloqueTexto("CARDI", 51f),
            BloqueTexto("eARDI", 28f),
            BloqueTexto("ZACCARDI", 60f),
            BloqueTexto("LucCARDI", 72f),
            BloqueTexto("Zuco", 39f),
            BloqueTexto("DNVYA LAN", 100f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("Zucc", 48f),
                BloqueTexto("9", 69f),
                BloqueTexto("acARDI", 91f),
                BloqueTexto("e", 2f),
                BloqueTexto("we o", 22f),
                BloqueTexto("CARDI", 84f),
                BloqueTexto("DNVYA LAN", 121f),
                BloqueTexto("LucCARDI", 102f),
                BloqueTexto("ARDI", 48f),
                BloqueTexto("ZACCARDI", 117f),
                BloqueTexto("2017", 57f),
                BloqueTexto("VALLE DEUCOARGENTINA", 268f),
                BloqueTexto("eARDI", 57f),
                BloqueTexto("MALBEC", 125f),
                BloqueTexto("ZUcCARDI", 222f),
                BloqueTexto("JOSÉ", 99f),
            ),
            listOf(
                BloqueTexto("JOSÉ", 89f),
                BloqueTexto("ZUcCARDI", 204f),
                BloqueTexto("MALBEC", 119f),
                BloqueTexto("VALLE DE UCOARGENTINA", 270f),
                BloqueTexto("2017", 57f),
                BloqueTexto("T", 31f),
                BloqueTexto("LUcCA", 73f),
                BloqueTexto("6RDE", 46f),
                BloqueTexto("ZucCARDI", 92f),
                BloqueTexto("CARDI", 77f),
                BloqueTexto("Zuc", 35f),
            ),
            ),
    )

    val lucaBesoDeDante = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("UCO VALLEY", 97f),
            BloqueTexto("(MENDO 2A", 101f),
            BloqueTexto("LUCA", 229f),
            BloqueTexto("BESO", 75f),
            BloqueTexto("DE DANTE", 145f),
            BloqueTexto("RCENYIND", 79f),
            BloqueTexto("NTA G", 63f),
            BloqueTexto("2021", 29f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("UCO VALLE) MENOO24", 140f),
                BloqueTexto("LUCA", 46f),
                BloqueTexto("BESO", 25f),
                BloqueTexto("DE DANTE", 30f),
                BloqueTexto("E. tgg9", 10f),
                BloqueTexto("RoENIINS", 21f),
                BloqueTexto("NI", 11f),
                BloqueTexto("2021", 20f),
            ),
            listOf(
                BloqueTexto("2021", 22f),
                BloqueTexto("NTA G", 13f),
                BloqueTexto("RCENYIND", 23f),
                BloqueTexto("DE DANTE", 35f),
                BloqueTexto("BESO", 28f),
                BloqueTexto("LUCA", 54f),
                BloqueTexto("(MENDO 2A", 63f),
                BloqueTexto("UCO VALLEY", 51f),
            ),
            ),
    )

    val chanarmuyo = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("CHANARMUYƠT", 574f),
            BloqueTexto("MALBEC 2023", 268f),
            BloqueTexto("RGEN", 103f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("CHANARMÜYO", 83f),
                BloqueTexto("MALBEC 2023", 38f),
                BloqueTexto("RGENT", 28f),
                BloqueTexto(", :YN", 18f),
            ),
            listOf(
                BloqueTexto("RGEN", 34f),
                BloqueTexto("MALBEC 2023", 41f),
                BloqueTexto("CHANARMUYƠT", 89f),
            ),
            ),
    )

    val sombrero = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("SOM", 114f),
            BloqueTexto("BRE", 80f),
            BloqueTexto("RO", 82f),
            BloqueTexto("CABERNET :", 182f),
            BloqueTexto("FRANO", 86f),
            BloqueTexto("2025", 54f),
            BloqueTexto("RUENTALA WINES", 134f),
            BloqueTexto("CND DE CCo", 121f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("TO", 14f),
                BloqueTexto("N", 7f),
                BloqueTexto("SOM", 56f),
                BloqueTexto("BRE", 43f),
                BloqueTexto("RO", 56f),
                BloqueTexto("CABERNET FRANC", 52f),
                BloqueTexto("2025", 31f),
                BloqueTexto("HUENTALA WINES", 17f),
                BloqueTexto("VALLE DE DCO", 20f),
                BloqueTexto("ENDOZA AROE", 13f),
            ),
            listOf(
                BloqueTexto("T", 6f),
                BloqueTexto("VALLE", 31f),
                BloqueTexto("DIN", 34f),
                BloqueTexto("ENDO", 17f),
                BloqueTexto("WUENTALA W", 19f),
                BloqueTexto("N", 11f),
                BloqueTexto("UC", 88f),
            ),
            ),
    )

    val antesYDespues = TextoEtiqueta(
        bloques = listOf(
            BloqueTexto("ZcCARDE", 16f),
            BloqueTexto("DE UO-MENDOZA-ARGENTO", 12f),
            BloqueTexto("NTES Y RESUE", 36f),
            BloqueTexto("CABERNET SAUVIGNON", 20f),
            BloqueTexto("MALBEC", 17f),
            BloqueTexto("2023", 17f),
            BloqueTexto("LUccL", 23f),
            BloqueTexto("GE", 21f),
        ),
        alternativas = listOf(
            listOf(
                BloqueTexto("LUccL", 64f),
                BloqueTexto("RDI", 27f),
                BloqueTexto("ZucCARDE", 86f),
                BloqueTexto("2023", 48f),
                BloqueTexto("MALBEC", 83f),
                BloqueTexto("GE", 29f),
                BloqueTexto("CABERNET SAUVIGNON", 214f),
                BloqueTexto("NTES Y DESTUE", 233f),
                BloqueTexto("N", 4f),
                BloqueTexto("DE CUO-MENDOZA-RCENTOS", 191f),
            ),
            listOf(
                BloqueTexto("4AN DE CUTO-MENDOZAARCENTO", 224f),
                BloqueTexto("WTES Y ESIUE", 237f),
                BloqueTexto("GF", 22f),
                BloqueTexto("CABERNET SAUVIGNON", 210f),
                BloqueTexto("MALBEC", 74f),
                BloqueTexto("2023", 48f),
                BloqueTexto("ZcCARDE", 86f),
                BloqueTexto("CCAF", 37f),
                BloqueTexto("RDI", 18f),
                BloqueTexto("LUcr", 46f),
            ),
            ),
    )

}
