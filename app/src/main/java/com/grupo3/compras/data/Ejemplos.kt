package com.grupo3.compras.data

import com.grupo3.compras.domain.Actividad
import com.grupo3.compras.domain.Actividad.CAMINANDO
import com.grupo3.compras.domain.Actividad.SENTADO
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Franja
import com.grupo3.compras.domain.Franja.FIN_DE_SEMANA
import com.grupo3.compras.domain.Franja.FUERA_DE_HORARIO
import com.grupo3.compras.domain.Franja.LABORAL
import com.grupo3.compras.domain.TipoInteraccion
import com.grupo3.compras.domain.TipoInteraccion.BUSQUEDA
import com.grupo3.compras.domain.TipoInteraccion.CLIC
import com.grupo3.compras.domain.TipoInteraccion.FAVORITO

/** Historiales de prueba para las demostraciones (escenarios de la diapositiva 15). */
object Ejemplos {
    private const val DIA_MS = 86_400_000.0

    private fun fila(
        ahora: Long, productoId: String, tipo: TipoInteraccion, diasAtras: Double,
        franja: Franja, actividad: Actividad, hora: Int
    ): InteraccionEntity {
        val producto = requireNotNull(Catalogo.porId(productoId)) { "Producto desconocido: $productoId" }
        return InteraccionEntity(
            productoId = productoId,
            categoria = producto.categoria.name,
            interaccion = tipo.name,
            fecha = ahora - (diasAtras * DIA_MS).toLong(),
            actividad = actividad.name,
            hora = hora,
            franja = franja.name,
            peso = tipo.peso
        )
    }

    private fun busqueda(
        ahora: Long, categoria: Categoria, termino: String, diasAtras: Double,
        franja: Franja, actividad: Actividad, hora: Int
    ) = InteraccionEntity(
        productoId = null,
        categoria = categoria.name,
        interaccion = BUSQUEDA.name,
        fecha = ahora - (diasAtras * DIA_MS).toLong(),
        actividad = actividad.name,
        hora = hora,
        franja = franja.name,
        peso = BUSQUEDA.peso,
        termino = termino
    )

    /** Persona que trabaja con tecnología y oficina entre semana, y se entretiene los fines de semana. */
    fun historialEjemplo(ahora: Long): List<InteraccionEntity> = listOf(
        fila(ahora, "te1", CLIC, 1.0, LABORAL, SENTADO, 10),
        fila(ahora, "te1", CLIC, 2.0, LABORAL, SENTADO, 11),
        fila(ahora, "te3", CLIC, 1.0, LABORAL, SENTADO, 15),
        fila(ahora, "te3", FAVORITO, 1.0, LABORAL, SENTADO, 15),
        fila(ahora, "te4", CLIC, 2.5, LABORAL, SENTADO, 9),
        fila(ahora, "te2", CLIC, 3.0, LABORAL, CAMINANDO, 9),
        fila(ahora, "of2", CLIC, 2.0, LABORAL, SENTADO, 14),
        fila(ahora, "of4", CLIC, 4.0, LABORAL, SENTADO, 10),
        busqueda(ahora, Categoria.OFICINA, "útiles", 4.0, LABORAL, SENTADO, 10),
        fila(ahora, "en1", CLIC, 2.0, FIN_DE_SEMANA, SENTADO, 20),
        fila(ahora, "en1", FAVORITO, 2.0, FIN_DE_SEMANA, SENTADO, 20),
        fila(ahora, "en4", CLIC, 3.0, FIN_DE_SEMANA, SENTADO, 16),
        fila(ahora, "en2", CLIC, 3.0, FIN_DE_SEMANA, CAMINANDO, 12),
        fila(ahora, "en6", CLIC, 5.0, FIN_DE_SEMANA, SENTADO, 17),
        fila(ahora, "de1", CLIC, 9.0, FUERA_DE_HORARIO, SENTADO, 19)
    )

    /** Antes le interesaba el deporte (hace 2-3 semanas); ahora solo mira tecnología. */
    fun cambioDeGustos(ahora: Long): List<InteraccionEntity> = listOf(
        fila(ahora, "de1", FAVORITO, 18.0, FUERA_DE_HORARIO, SENTADO, 19),
        fila(ahora, "de1", CLIC, 17.0, FUERA_DE_HORARIO, SENTADO, 20),
        fila(ahora, "de3", CLIC, 17.0, FUERA_DE_HORARIO, SENTADO, 20),
        fila(ahora, "de3", CLIC, 16.0, FUERA_DE_HORARIO, SENTADO, 19),
        fila(ahora, "de2", CLIC, 15.0, FUERA_DE_HORARIO, CAMINANDO, 18),
        fila(ahora, "de4", CLIC, 15.0, FUERA_DE_HORARIO, SENTADO, 21),
        busqueda(ahora, Categoria.DEPORTE, "correr", 14.0, FUERA_DE_HORARIO, SENTADO, 19),
        fila(ahora, "te1", CLIC, 0.5, FUERA_DE_HORARIO, SENTADO, 21),
        fila(ahora, "te1", FAVORITO, 0.5, FUERA_DE_HORARIO, SENTADO, 21),
        fila(ahora, "te2", CLIC, 0.3, FUERA_DE_HORARIO, SENTADO, 20),
        fila(ahora, "te3", CLIC, 0.2, FUERA_DE_HORARIO, SENTADO, 20),
        busqueda(ahora, Categoria.TECNOLOGIA, "laptop", 0.1, FUERA_DE_HORARIO, SENTADO, 20)
    )

    /** Los productos marcados como favorito en un historial de ejemplo. */
    fun favoritosDe(filas: List<InteraccionEntity>): List<FavoritoEntity> =
        filas.filter { it.interaccion == FAVORITO.name && it.productoId != null }
            .map { FavoritoEntity(it.productoId!!, it.fecha) }
}
