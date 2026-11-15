package com.grupo3.compras.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

enum class TipoInteraccion(val peso: Int) { BUSQUEDA(1), CLIC(2), FAVORITO(3) }
enum class Categoria { OFICINA, ENTRETENIMIENTO, OTROS }

data class Producto(val id: String, val nombre: String, val categoria: Categoria)
data class Interaccion(val productoId: String, val tipo: TipoInteraccion, val fechaHora: LocalDateTime)
data class Recomendacion(val producto: Producto, val puntaje: Double, val justificacion: String)

fun contextoAhora(epochMillis: Long = System.currentTimeMillis()): LocalDateTime =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Recomendador local de productos (Grupo 3 · Compras inteligentes).
 * Registrar -> Calcular -> Contextualizar -> Mostrar 3 recomendaciones.
 * Sin red: todo se calcula con datos del dispositivo.
 */
class RecomendadorCompras(
    private val factorBonoContexto: Double = 1.5,
    private val penalizacionReciente: Double = 0.5,
    private val maxRecientes: Int = 6
) {
    private val mostradosRecientemente = ArrayDeque<String>()

    fun puntajesHistoricos(interacciones: List<Interaccion>): Map<String, Double> =
        interacciones.groupBy { it.productoId }
            .mapValues { (_, l) -> l.sumOf { it.tipo.peso.toDouble() } }

    private fun esHorarioLaboral(f: LocalDateTime) =
        f.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) && f.hour in 9..17

    private fun esFinDeSemana(f: LocalDateTime) =
        f.dayOfWeek == DayOfWeek.SATURDAY || f.dayOfWeek == DayOfWeek.SUNDAY

    private fun bono(p: Producto, f: LocalDateTime): Double = when {
        p.categoria == Categoria.OFICINA && esHorarioLaboral(f) -> factorBonoContexto
        p.categoria == Categoria.ENTRETENIMIENTO && esFinDeSemana(f) -> factorBonoContexto
        else -> 1.0
    }

    fun recomendar(
        catalogo: List<Producto>,
        interacciones: List<Interaccion>,
        ahora: LocalDateTime,
        cantidad: Int = 3
    ): List<Recomendacion> {
        val historico = puntajesHistoricos(interacciones)

        val elegidos = catalogo.map { p ->
            val b = bono(p, ahora)
            val penal = if (p.id in mostradosRecientemente) penalizacionReciente else 1.0
            Triple(p, (historico[p.id] ?: 0.0) * b * penal, Pair(b, penal))
        }.sortedByDescending { it.second }
            .take(cantidad)

        elegidos.forEach {
            mostradosRecientemente.addLast(it.first.id)
            while (mostradosRecientemente.size > maxRecientes) mostradosRecientemente.removeFirst()
        }

        return elegidos.map { (p, puntaje, factores) ->
            val partes = mutableListOf("Histórico ${historico[p.id] ?: 0.0} pts")
            if (factores.first > 1.0) partes += "bono de contexto x${factores.first}"
            if (factores.second < 1.0) partes += "penalizado por mostrarse hace poco"
            Recomendacion(p, puntaje, partes.joinToString(" · "))
        }
    }
}
