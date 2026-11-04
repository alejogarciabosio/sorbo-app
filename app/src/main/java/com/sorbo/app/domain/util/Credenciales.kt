// Capa de dominio · utilidad: validaciones puras de lo que se escribe al crear una cuenta o iniciar sesión.
package com.sorbo.app.domain.util

/** Cantidad mínima de caracteres de la contraseña. Debe coincidir con la que se configure en Supabase Auth. */
const val LARGO_MINIMO_CLAVE = 8

private val FORMATO_EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

fun esEmailValido(texto: String): Boolean = FORMATO_EMAIL.matches(texto.trim())

fun esClaveValida(clave: String): Boolean = clave.length >= LARGO_MINIMO_CLAVE
