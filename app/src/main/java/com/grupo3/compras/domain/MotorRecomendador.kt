package com.grupo3.compras.domain

import kotlin.math.pow

/**
 * Motor de recomendación 100 % local (on-device) del Grupo 3 · Compras inteligentes.
 *
 * Pasos (diapositiva 13): Registrar -> Calcular -> Contextualizar -> Mostrar 3 recomendaciones.
 *  - Calcular: puntaje histórico por categoría (suma de pesos con decaimiento en el tiempo).
 *  - Contextualizar: Naive Bayes P(categoría | franja, actividad) + reglas del grupo
 *    (horario laboral -> oficina, fin de semana -> entretenimiento).
 *  - Variedad: penaliza lo mostrado recientemente y repite menos la misma categoría.
 */
class MotorRecomendador(
    private val semividaDias: Double = 7.0,
    private val alfa: Double = 1.0
) {
    companion object {
        const val PESO_NAIVE_BAYES = 0.35
        const val PESO_HISTORICO = 0.25
        const val PESO_PRODUCTO = 0.12
        const val PESO_REGLA = 0.35
        const val PESO_POPULARIDAD = 0.05
        const val BONO_OFERTA_CAMINANDO = 0.10
        const val PENALIZACION_MOSTRADO = 0.55
        const val PENALIZACION_CATEGORIA_REPETIDA = 0.8
        const val MIN_INTERACCIONES_DESCUBRIMIENTO = 3
        private const val MS_POR_DIA = 86_400_000.0
    }

    private data class Candidato(
        val producto: Producto,
        val puntaje: Double,
        val afinidad: Double,
        val regla: Double,
        val loViste: Boolean,
        val oferta: Boolean
    )

    /** Peso de una interacción: su peso base, reducido a la mitad cada [semividaDias] días. */
    fun pesoEfectivo(i: Interaccion, ahora: Long): Double {
        val dias = (ahora - i.epochMillis).coerceAtLeast(0L) / MS_POR_DIA
        return i.peso * 0.5.pow(dias / semividaDias)
    }

    /** Línea base: puntaje histórico acumulado por categoría. */
    fun puntajesHistoricos(historial: List<Interaccion>, ahora: Long): Map<Categoria, Double> {
        val acumulado = Categoria.entries.associateWith { 0.0 }.toMutableMap()
        for (i in historial) {
            acumulado[i.categoria] = acumulado.getValue(i.categoria) + pesoEfectivo(i, ahora)
        }
        return acumulado
    }

    /**
     * Naive Bayes con suavizado de Laplace:
     * P(categoría | franja, actividad) ∝ P(categoría) · P(franja | categoría) · P(actividad | categoría).
     * Las probabilidades se calculan con conteos ponderados. Sin historial todas valen lo mismo.
     */
    fun probabilidadesNaiveBayes(
        historial: List<Interaccion>,
        franja: Franja,
        actividad: Actividad,
        ahora: Long
    ): Map<Categoria, Double> {
        val ponderadas = historial.map { it to pesoEfectivo(it, ahora) }
        val total = ponderadas.sumOf { it.second }
        val k = Categoria.entries.size
        val posterior = Categoria.entries.associateWith { c ->
            val deLaCategoria = ponderadas.filter { it.first.categoria == c }
            val nC = deLaCategoria.sumOf { it.second }
            val nCF = deLaCategoria.filter { it.first.franja == franja }.sumOf { it.second }
            val nCA = deLaCategoria.filter { it.first.actividad == actividad }.sumOf { it.second }
            val prior = (nC + alfa) / (total + alfa * k)
            val pFranja = (nCF + alfa) / (nC + alfa * Franja.entries.size)
            val pActividad = (nCA + alfa) / (nC + alfa * Actividad.entries.size)
            prior * pFranja * pActividad
        }
        val z = posterior.values.sum()
        return posterior.mapValues { it.value / z }
    }

    /** Reglas del grupo: qué categorías se empujan según la franja horaria. */
    fun regla(franja: Franja, categoria: Categoria): Double = when (franja) {
        Franja.LABORAL -> if (categoria == Categoria.OFICINA) 1.0 else 0.0
        Franja.FIN_DE_SEMANA -> if (categoria == Categoria.ENTRETENIMIENTO) 1.0 else 0.0
        Franja.FUERA_DE_HORARIO ->
            if (categoria == Categoria.ENTRETENIMIENTO || categoria == Categoria.HOGAR) 0.5 else 0.0
    }

    fun recomendar(
        catalogo: List<Producto>,
        historial: List<Interaccion>,
        favoritos: Set<String>,
        franja: Franja,
        actividad: Actividad,
        ahora: Long,
        mostradosRecientes: List<String> = emptyList(),
        cantidad: Int = 3
    ): ResultadoMotor {
        val historico = puntajesHistoricos(historial, ahora)
        val maxHistorico = historico.values.maxOrNull() ?: 0.0
        val nb = probabilidadesNaiveBayes(historial, franja, actividad, ahora)
        val maxNb = nb.values.maxOrNull() ?: 1.0

        val interesPorProducto: Map<String, Double> = historial
            .filter { it.productoId != null }
            .groupBy { it.productoId!! }
            .mapValues { (_, lista) -> lista.sumOf { pesoEfectivo(it, ahora) } }
        val maxProducto = interesPorProducto.values.maxOrNull() ?: 0.0
        val categoriasConHistorial = historial.map { it.categoria }.toSet()

        val candidatos = catalogo.filter { it.id !in favoritos }.map { p ->
            val c = p.categoria
            val afinidad = if (maxHistorico > 0) historico.getValue(c) / maxHistorico else 0.0
            val contextual = if (maxNb > 0) nb.getValue(c) / maxNb else 0.0
            val interes = if (maxProducto > 0) (interesPorProducto[p.id] ?: 0.0) / maxProducto else 0.0
            val reg = regla(franja, c)
            val oferta = if (actividad == Actividad.CAMINANDO && p.enOferta) BONO_OFERTA_CAMINANDO else 0.0
            val base = PESO_NAIVE_BAYES * contextual +
                PESO_HISTORICO * afinidad +
                PESO_PRODUCTO * interes +
                PESO_REGLA * reg +
                PESO_POPULARIDAD * (p.popularidad / 100.0) +
                oferta
            val veces = mostradosRecientes.count { it == p.id }
            Candidato(
                producto = p,
                puntaje = base * PENALIZACION_MOSTRADO.pow(veces),
                afinidad = afinidad,
                regla = reg,
                loViste = (interesPorProducto[p.id] ?: 0.0) > 0.0,
                oferta = oferta > 0.0
            )
        }

        val elegidos = mutableListOf<Pair<Candidato, Boolean>>()
        val restantes = candidatos.toMutableList()
        for (indice in 0 until cantidad) {
            if (restantes.isEmpty()) break
            val ultimoLugar = indice == cantidad - 1
            val descubrir = ultimoLugar && historial.size >= MIN_INTERACCIONES_DESCUBRIMIENTO
            val nuevas: List<Candidato>? = if (descubrir) {
                restantes.filter { it.producto.categoria !in categoriasConHistorial }.ifEmpty { null }
            } else null
            val fuente = nuevas ?: restantes
            val mejor = fuente.maxByOrNull { c ->
                val repetidas = elegidos.count { it.first.producto.categoria == c.producto.categoria }
                c.puntaje * PENALIZACION_CATEGORIA_REPETIDA.pow(repetidas)
            } ?: break
            elegidos += mejor to (nuevas != null)
            restantes.remove(mejor)
        }

        val recomendaciones = elegidos.map { (c, descubrimiento) ->
            Recomendacion(
                producto = c.producto,
                puntaje = c.puntaje,
                razones = razones(c, franja, descubrimiento, historial.isEmpty()),
                descubrimiento = descubrimiento
            )
        }
        val analisis = Categoria.entries
            .map { AnalisisCategoria(it, historico.getValue(it), nb.getValue(it), regla(franja, it)) }
            .sortedByDescending { it.historico }
        return ResultadoMotor(recomendaciones, analisis)
    }

    private fun razones(c: Candidato, franja: Franja, descubrimiento: Boolean, sinHistorial: Boolean): List<String> {
        val r = mutableListOf<String>()
        if (c.regla > 0) {
            r += when (franja) {
                Franja.LABORAL -> "💼 Horario laboral"
                Franja.FIN_DE_SEMANA -> "🎉 Fin de semana"
                Franja.FUERA_DE_HORARIO -> "🌙 Para tu noche"
            }
        }
        if (c.afinidad >= 0.4) r += "Te interesa ${c.producto.categoria.etiqueta}"
        if (c.loViste) r += "Lo viste antes"
        if (c.oferta) r += "🚶 Oferta rápida −${c.producto.descuento}%"
        if (descubrimiento) r += "✨ Para descubrir"
        if (sinHistorial && r.isEmpty()) r += "⭐ Popular ahora"
        if (r.isEmpty()) r += "Selección para ti"
        return r
    }
}
