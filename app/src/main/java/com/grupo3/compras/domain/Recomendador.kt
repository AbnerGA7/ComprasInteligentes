package com.grupo3.compras.domain

import kotlin.math.pow

/**
 * Recomendador local (sin Internet) de productos.
 *
 * 1. Aprende: suma los pesos de las búsquedas (1), clics (2) y favoritos (3) por categoría y por producto.
 *    Las acciones recientes pesan más: cada [semividaDias] días un punto vale la mitad.
 * 2. Contexto: en horario laboral reserva lugares para artículos de oficina y en fin de semana
 *    para productos de entretenimiento.
 * 3. Variedad: lo que se mostró hace poco baja de posición, así no se repiten siempre los mismos.
 */
class Recomendador(private val semividaDias: Double = 7.0) {

    companion object {
        /** Cuántos de los 3 lugares se reservan para la categoría del contexto. */
        const val LUGARES_DEL_CONTEXTO = 2
        const val MAX_POR_CATEGORIA = 2
        private const val MS_POR_DIA = 86_400_000.0
    }

    private fun peso(i: Interaccion, ahora: Long): Double {
        val dias = (ahora - i.epochMillis).coerceAtLeast(0L) / MS_POR_DIA
        return i.tipo.peso * 0.5.pow(dias / semividaDias)
    }

    fun puntosPorCategoria(historial: List<Interaccion>, ahora: Long): Map<Categoria, Double> {
        val puntos = Categoria.entries.associateWith { 0.0 }.toMutableMap()
        for (i in historial) puntos[i.categoria] = puntos.getValue(i.categoria) + peso(i, ahora)
        return puntos
    }

    fun categoriaDelContexto(franja: Franja): Categoria? = when (franja) {
        Franja.LABORAL -> Categoria.OFICINA
        Franja.FIN_DE_SEMANA -> Categoria.ENTRETENIMIENTO
        Franja.OTRO -> null
    }

    fun recomendar(
        catalogo: List<Producto>,
        historial: List<Interaccion>,
        favoritos: Set<String>,
        franja: Franja,
        ahora: Long,
        mostradosRecientes: List<String> = emptyList(),
        cantidad: Int = 3
    ): List<Recomendacion> {
        val puntos = puntosPorCategoria(historial, ahora)
        val maxPuntos = puntos.values.maxOrNull() ?: 0.0
        val interes: Map<String, Double> = historial
            .filter { it.productoId != null }
            .groupBy { it.productoId!! }
            .mapValues { (_, lista) -> lista.sumOf { peso(it, ahora) } }
        val maxInteres = interes.values.maxOrNull() ?: 0.0

        fun puntaje(p: Producto): Double {
            val gustoProducto = if (maxInteres > 0) (interes[p.id] ?: 0.0) / maxInteres else 0.0
            val gustoCategoria = if (maxPuntos > 0) puntos.getValue(p.categoria) / maxPuntos else 0.0
            val base = 0.5 * gustoProducto + 0.3 * gustoCategoria + 0.2 * (p.popularidad / 100.0)
            val vecesMostrado = mostradosRecientes.count { it == p.id }
            return base * 0.5.pow(vecesMostrado)
        }

        fun conVisto(motivo: String, p: Producto) =
            if ((interes[p.id] ?: 0.0) > 0) "$motivo · ya lo viste" else motivo

        fun motivoDeGusto(p: Producto) =
            if (puntos.getValue(p.categoria) > 0) conVisto("Te interesa ${p.categoria.etiqueta}", p)
            else conVisto("Producto popular", p)

        val disponibles = catalogo.filter { it.id !in favoritos }.sortedByDescending { puntaje(it) }
        val categoriaContexto = categoriaDelContexto(franja)
        val elegidos = mutableListOf<Recomendacion>()

        // 1) Lo que pide el contexto: oficina en horario laboral, entretenimiento en fin de semana.
        if (categoriaContexto != null) {
            val motivo = "${franja.etiqueta}: ${categoriaContexto.etiqueta.lowercase()}"
            disponibles.filter { it.categoria == categoriaContexto }
                .take(minOf(LUGARES_DEL_CONTEXTO, cantidad))
                .forEach { elegidos += Recomendacion(it, conVisto(motivo, it)) }
        }

        // 2) El resto, según lo que más le gusta (máximo 2 por categoría para dar variedad).
        for (p in disponibles) {
            if (elegidos.size >= cantidad) break
            if (elegidos.any { it.producto.id == p.id }) continue
            if (categoriaContexto != null && p.categoria == categoriaContexto) continue
            if (elegidos.count { it.producto.categoria == p.categoria } >= MAX_POR_CATEGORIA) continue
            elegidos += Recomendacion(p, motivoDeGusto(p))
        }

        // 3) Si el catálogo casi se agotó, se completa con lo que quede.
        for (p in disponibles) {
            if (elegidos.size >= cantidad) break
            if (elegidos.none { it.producto.id == p.id }) elegidos += Recomendacion(p, motivoDeGusto(p))
        }
        return elegidos
    }
}
