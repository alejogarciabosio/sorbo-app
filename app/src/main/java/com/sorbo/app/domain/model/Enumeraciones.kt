package com.sorbo.app.domain.model

// Listas cerradas: permiten calcular estadísticas confiables en "Mis gustos".

enum class Dulzor { DULCE, SECO }

enum class Comida { CARNE, PASTA, PESCADO, PIZZA, QUESOS, PICADA, POSTRE, SIN_COMIDA, OTRA }

enum class Compania { SOLO, PAREJA, AMIGOS, FAMILIA, TRABAJO, OTRA }

enum class Ocasion { CENA, ASADO, PREVIA, ALMUERZO, CELEBRACION, OTRA }

/** Si el registro ya está respaldado en la nube o todavía no. */
enum class EstadoSync { PENDIENTE, SINCRONIZADO }
