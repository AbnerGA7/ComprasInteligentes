package com.grupo3.compras.domain

/** Una acción del usuario resumida para Naive Bayes: categoría, contexto en que ocurrió y su peso (ya con el olvido aplicado). */
class Observacion(val categoria: Categoria, val contexto: Contexto, val peso: Double)

/**
 * Naive Bayes sobre categorías:
 *
 *   P(Categoría | Contexto)  ∝  P(Categoría) × P(Día | Categoría) × P(Momento | Categoría)
 *
 * - P(Categoría): qué parte de tus puntos son de esa categoría.
 * - P(Día | Categoría) y P(Momento | Categoría): de las veces que interactuaste con esa categoría, cuántas fueron en ese día y momento.
 * - "Ingenuo" porque multiplica día y momento como si no tuvieran relación entre sí.
 * - Suavizado de Laplace ([alfa]) para que nunca haya una probabilidad de 0 con pocos datos.
 *
 * En vez de contar cada acción como 1, cuenta su peso (búsqueda 1, clic 2, favorito 3, con olvido), así que
 * lo que importa más y lo más reciente influye más en la probabilidad.
 */
object NaiveBayes {
    fun probabilidades(
        observaciones: List<Observacion>,
        contexto: Contexto,
        alfa: Double = 1.0
    ): Map<Categoria, Double> {
        val total = observaciones.sumOf { it.peso }
        val categorias = Categoria.entries
        val sinNormalizar = categorias.associateWith { categoria ->
            val deLaCategoria = observaciones.filter { it.categoria == categoria }
            val pesoCategoria = deLaCategoria.sumOf { it.peso }
            val pesoDia = deLaCategoria.filter { it.contexto.dia == contexto.dia }.sumOf { it.peso }
            val pesoMomento = deLaCategoria.filter { it.contexto.momento == contexto.momento }.sumOf { it.peso }

            val previa = (pesoCategoria + alfa) / (total + alfa * categorias.size)
            val verosimilitudDia = (pesoDia + alfa) / (pesoCategoria + alfa * TipoDia.entries.size)
            val verosimilitudMomento = (pesoMomento + alfa) / (pesoCategoria + alfa * Momento.entries.size)
            previa * verosimilitudDia * verosimilitudMomento
        }
        val suma = sinNormalizar.values.sum()
        return sinNormalizar.mapValues { it.value / suma }
    }
}
