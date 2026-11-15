package com.grupo3.compras.domain

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Recomendador local (sin Internet) de productos.
 *
 * 1. Aprende: suma los pesos de las búsquedas (1), clics (2) y favoritos (3) por categoría y por producto.
 *    Las acciones recientes pesan más: cada [semividaDias] días un punto vale la mitad.
 * 2. Parecidos: lo que buscas, abres o guardas hace subir a los productos que comparten etiquetas con ellos.
 * 3. Contexto: en horario laboral reserva lugares para artículos de oficina y en fin de semana
 *    para productos de entretenimiento. Además, Naive Bayes estima qué categoría es más probable
 *    según el día y el momento del día, y esa probabilidad entra al puntaje de cada producto.
 * 4. Variedad: lo que se mostró hace poco baja de posición, así no se repiten siempre los mismos.
 */
class Recomendador(private val semividaDias: Double = 7.0) {

    companion object {
        /** Cuántos de los 3 lugares se reservan para la categoría del contexto. */
        const val LUGARES_DEL_CONTEXTO = 2
        const val MAX_POR_CATEGORIA = 2
        private const val MS_POR_DIA = 86_400_000.0
    }

    /** Cuánto se parece un producto a lo que el usuario ya buscó, abrió o guardó, y a cuál de esos se parece más. */
    private class Parecido(val total: Double, val origen: Producto?)

    private fun peso(i: Interaccion, ahora: Long): Double {
        val dias = (ahora - i.epochMillis).coerceAtLeast(0L) / MS_POR_DIA
        return i.tipo.peso * 0.5.pow(dias / semividaDias)
    }

    fun puntosPorCategoria(historial: List<Interaccion>, ahora: Long): Map<Categoria, Double> {
        val puntos = Categoria.entries.associateWith { 0.0 }.toMutableMap()
        for (i in historial) puntos[i.categoria] = puntos.getValue(i.categoria) + peso(i, ahora)
        return puntos
    }

    /** Naive Bayes: probabilidad de que le interese cada categoría en este contexto (suman 1). */
    fun probabilidadesPorCategoria(
        historial: List<Interaccion>,
        contexto: Contexto,
        ahora: Long
    ): Map<Categoria, Double> =
        NaiveBayes.probabilidades(
            historial.map { Observacion(it.categoria, it.contexto, peso(it, ahora)) },
            contexto
        )

    fun categoriaDelContexto(franja: Franja): Categoria? = when (franja) {
        Franja.LABORAL -> Categoria.OFICINA
        Franja.FIN_DE_SEMANA -> Categoria.ENTRETENIMIENTO
        Franja.OTRO -> null
    }

    // ------------------------------------------------------------------ interés y parecidos

    private fun interesPorProducto(historial: List<Interaccion>, ahora: Long): Map<String, Double> =
        historial
            .filter { it.productoId != null }
            .groupBy { it.productoId!! }
            .mapValues { (_, lista) -> lista.sumOf { peso(it, ahora) } }

    /** La acción más fuerte que hizo el usuario con cada producto: favorito, luego clic, luego búsqueda. */
    private fun accionPrincipal(historial: List<Interaccion>): Map<String, TipoInteraccion> =
        historial
            .filter { it.productoId != null }
            .groupBy { it.productoId!! }
            .mapValues { (_, lista) -> lista.maxBy { it.tipo.peso }.tipo }

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val comunes = a.count { it in b }
        return comunes.toDouble() / (a.size + b.size - comunes)
    }

    private fun parecidos(catalogo: List<Producto>, interes: Map<String, Double>): Map<String, Parecido> {
        val porId = catalogo.associateBy { it.id }
        return catalogo.associate { candidato ->
            var total = 0.0
            var mejorAporte = 0.0
            var origen: Producto? = null
            for ((id, pesoDelInteres) in interes) {
                val visto = porId[id] ?: continue
                if (visto.id == candidato.id) continue
                val aporte = pesoDelInteres * jaccard(visto.etiquetas, candidato.etiquetas)
                total += aporte
                if (aporte > mejorAporte) {
                    mejorAporte = aporte
                    origen = visto
                }
            }
            candidato.id to Parecido(total, origen)
        }
    }

    private fun motivoParecido(origen: Producto, accion: TipoInteraccion?): String = when (accion) {
        TipoInteraccion.FAVORITO -> "Parecido a tu favorito: ${origen.nombre}"
        TipoInteraccion.BUSQUEDA -> "Parecido a lo que buscaste: ${origen.nombre}"
        else -> "Parecido a lo que viste: ${origen.nombre}"
    }

    // ------------------------------------------------------------------ recomendaciones

    fun recomendar(
        catalogo: List<Producto>,
        historial: List<Interaccion>,
        favoritos: Set<String>,
        franja: Franja,
        contexto: Contexto,
        ahora: Long,
        mostradosRecientes: List<String> = emptyList(),
        cantidad: Int = 3
    ): List<Recomendacion> {
        val puntos = puntosPorCategoria(historial, ahora)
        val probabilidades = probabilidadesPorCategoria(historial, contexto, ahora)
        val interes = interesPorProducto(historial, ahora)
        val acciones = accionPrincipal(historial)
        val maxInteres = interes.values.maxOrNull() ?: 0.0
        val parecidos = parecidos(catalogo, interes)
        val maxParecido = parecidos.values.maxOfOrNull { it.total } ?: 0.0

        fun parecidoNormalizado(p: Producto): Double =
            if (maxParecido > 0) parecidos.getValue(p.id).total / maxParecido else 0.0

        // 35 % interés en el producto + 25 % parecido a lo que te interesó + 25 % probabilidad (Naive Bayes)
        // de su categoría + 15 % popularidad. Lo mostrado hace poco vale la mitad por cada vez.
        fun puntaje(p: Producto): Double {
            val gustoProducto = if (maxInteres > 0) (interes[p.id] ?: 0.0) / maxInteres else 0.0
            val base = 0.35 * gustoProducto +
                0.25 * parecidoNormalizado(p) +
                0.25 * probabilidades.getValue(p.categoria) +
                0.15 * (p.popularidad / 100.0)
            val vecesMostrado = mostradosRecientes.count { it == p.id }
            return base * 0.5.pow(vecesMostrado)
        }

        fun conVisto(motivo: String, p: Producto) =
            if ((interes[p.id] ?: 0.0) > 0) "$motivo · ya lo viste" else motivo

        fun motivoDeGusto(p: Producto): String {
            val parecido = parecidos.getValue(p.id)
            val origen = parecido.origen
            if (origen != null && parecidoNormalizado(p) >= 0.5) {
                return motivoParecido(origen, acciones[origen.id])
            }
            if (puntos.getValue(p.categoria) <= 0) return "Producto popular"
            val porcentaje = (probabilidades.getValue(p.categoria) * 100).roundToInt()
            val loViste = (interes[p.id] ?: 0.0) > 0
            // Si la categoría casi no encaja con este momento, pero ya viste el producto, se dice solo eso.
            return if (loViste && porcentaje < 15) "Lo viste antes"
            else conVisto("Te interesa ${p.categoria.etiqueta} ($porcentaje % ahora)", p)
        }

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

    /**
     * Productos nuevos parecidos a lo que el usuario buscó, abrió o guardó, sin importar la hora.
     * No incluye favoritos, lo que ya tocó ni lo que ya está en las recomendaciones principales ([excluir]).
     */
    fun relacionados(
        catalogo: List<Producto>,
        historial: List<Interaccion>,
        favoritos: Set<String>,
        excluir: Set<String>,
        ahora: Long,
        cantidad: Int = 3
    ): List<Recomendacion> {
        val interes = interesPorProducto(historial, ahora)
        val acciones = accionPrincipal(historial)
        val parecidos = parecidos(catalogo, interes)
        val candidatos = catalogo
            .filter { it.id !in favoritos && it.id !in excluir && it.id !in interes }
            .mapNotNull { p ->
                val parecido = parecidos.getValue(p.id)
                val origen = parecido.origen
                if (parecido.total > 0 && origen != null) Triple(p, parecido.total, origen) else null
            }
            .sortedByDescending { it.second }

        val elegidos = mutableListOf<Triple<Producto, Double, Producto>>()
        // 1) Uno por cada producto distinto que tocó, para que lo último que hizo siempre se note.
        for (c in candidatos) {
            if (elegidos.size >= cantidad) break
            if (elegidos.none { it.third.id == c.third.id }) elegidos += c
        }
        // 2) Si sobran lugares, los mejores que queden.
        for (c in candidatos) {
            if (elegidos.size >= cantidad) break
            if (c !in elegidos) elegidos += c
        }
        return elegidos
            .sortedByDescending { it.second }
            .map { (p, _, origen) -> Recomendacion(p, motivoParecido(origen, acciones[origen.id])) }
    }
}
