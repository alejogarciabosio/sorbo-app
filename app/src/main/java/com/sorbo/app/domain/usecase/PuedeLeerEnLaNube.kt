// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.ConectividadObserver
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.repository.SesionRepository
import kotlinx.coroutines.flow.first

/**
 * La foto de la etiqueta solo sale del teléfono si se cumplen las cuatro condiciones (RNF06): hay sesión iniciada,
 * la persona aceptó el aviso de la IA, no desactivó la IA en Ajustes y hay conexión.
 */
class PuedeLeerEnLaNube(
    private val sesion: SesionRepository,
    private val preferencias: PreferenciasRepository,
    private val conectividad: ConectividadObserver,
) {
    suspend operator fun invoke(): Boolean =
        sesion.sesion.first() is EstadoSesion.ConSesion &&
            preferencias.avisoIaAceptado.first() &&
            preferencias.iaActivada.first() &&
            conectividad.hayConexion.first()
}
