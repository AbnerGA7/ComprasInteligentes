package com.grupo3.compras.domain

import com.grupo3.compras.data.Catalogo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorRecomendadorTest {
    private val motor = MotorRecomendador()
    private val ahora = 1_800_000_000_000L
    private val diaMs = 86_400_000.0

    private fun interaccion(
        productoId: String?,
        categoria: Categoria,
        tipo: TipoInteraccion,
        diasAtras: Double = 0.0,
        franja: Franja = Franja.LABORAL,
        actividad: Actividad = Actividad.SENTADO
    ) = Interaccion(productoId, categoria, tipo, ahora - (diasAtras * diaMs).toLong(), actividad, franja, tipo.peso)

    private fun recomendar(
        historial: List<Interaccion> = emptyList(),
        franja: Franja,
        actividad: Actividad = Actividad.SENTADO,
        favoritos: Set<String> = emptySet(),
        mostrados: List<String> = emptyList()
    ) = motor.recomendar(Catalogo.productos, historial, favoritos, franja, actividad, ahora, mostrados)

    @Test
    fun usuarioNuevoEnHorarioLaboralRecibeOficina() {
        val recs = recomendar(franja = Franja.LABORAL).recomendaciones
        assertEquals(3, recs.size)
        assertEquals(Categoria.OFICINA, recs.first().producto.categoria)
        assertTrue(recs.count { it.producto.categoria == Categoria.OFICINA } >= 2)
    }

    @Test
    fun usuarioNuevoEnFinDeSemanaRecibeEntretenimiento() {
        val recs = recomendar(franja = Franja.FIN_DE_SEMANA).recomendaciones
        assertEquals(Categoria.ENTRETENIMIENTO, recs.first().producto.categoria)
    }

    @Test
    fun caminandoSePriorizanOfertasRapidas() {
        val recs = recomendar(franja = Franja.FUERA_DE_HORARIO, actividad = Actividad.CAMINANDO).recomendaciones
        assertTrue(recs.first().producto.enOferta)
        assertTrue(recs.first().razones.any { it.startsWith("🚶") })
    }

    @Test
    fun losFavoritosNoSeVuelvenARecomendar() {
        val favoritos = setOf("of2", "of3", "of1")
        val recs = recomendar(franja = Franja.LABORAL, favoritos = favoritos).recomendaciones
        assertTrue(recs.none { it.producto.id in favoritos })
    }

    @Test
    fun loMostradoRecientementeSeRotaYNoSeRepite() {
        val primera = recomendar(franja = Franja.LABORAL).recomendaciones.map { it.producto.id }
        val segunda = recomendar(franja = Franja.LABORAL, mostrados = primera).recomendaciones.map { it.producto.id }
        assertTrue("Se repitieron: ${primera.intersect(segunda.toSet())}", primera.intersect(segunda.toSet()).isEmpty())
    }

    @Test
    fun elPesoSeReducePorLaMitadCadaSemana() {
        val clicHaceUnaSemana = interaccion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC, diasAtras = 7.0)
        assertEquals(1.0, motor.pesoEfectivo(clicHaceUnaSemana, ahora), 1e-9)
        val clicHoy = interaccion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC)
        assertEquals(2.0, motor.pesoEfectivo(clicHoy, ahora), 1e-9)
    }

    @Test
    fun elCambioDeGustosPesaMasQueElPasado() {
        val historial = List(5) {
            interaccion("de1", Categoria.DEPORTE, TipoInteraccion.FAVORITO, diasAtras = 30.0)
        } + List(2) {
            interaccion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC)
        }
        val puntajes = motor.puntajesHistoricos(historial, ahora)
        assertTrue(puntajes.getValue(Categoria.TECNOLOGIA) > puntajes.getValue(Categoria.DEPORTE))
    }

    @Test
    fun naiveBayesSumaUnoYSigueAlContexto() {
        val historial = List(6) {
            interaccion("en1", Categoria.ENTRETENIMIENTO, TipoInteraccion.CLIC, franja = Franja.FIN_DE_SEMANA)
        } + List(6) {
            interaccion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC, franja = Franja.LABORAL)
        }
        val finDeSemana = motor.probabilidadesNaiveBayes(historial, Franja.FIN_DE_SEMANA, Actividad.SENTADO, ahora)
        val laboral = motor.probabilidadesNaiveBayes(historial, Franja.LABORAL, Actividad.SENTADO, ahora)
        assertEquals(1.0, finDeSemana.values.sum(), 1e-9)
        assertTrue(finDeSemana.getValue(Categoria.ENTRETENIMIENTO) > finDeSemana.getValue(Categoria.TECNOLOGIA))
        assertTrue(laboral.getValue(Categoria.TECNOLOGIA) > laboral.getValue(Categoria.ENTRETENIMIENTO))
    }

    @Test
    fun sinHistorialNaiveBayesEsUniforme() {
        val nb = motor.probabilidadesNaiveBayes(emptyList(), Franja.LABORAL, Actividad.SENTADO, ahora)
        nb.values.forEach { assertEquals(1.0 / Categoria.entries.size, it, 1e-9) }
    }

    @Test
    fun elUltimoLugarEsUnDescubrimientoCuandoYaHayHistorial() {
        val historial = List(4) {
            interaccion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC, franja = Franja.FUERA_DE_HORARIO)
        }
        val recs = recomendar(historial, franja = Franja.FUERA_DE_HORARIO).recomendaciones
        val ultimo = recs.last()
        assertTrue(ultimo.descubrimiento)
        assertNotEquals(Categoria.TECNOLOGIA, ultimo.producto.categoria)
        assertFalse(recs.dropLast(1).any { it.descubrimiento })
    }

    @Test
    fun todaRecomendacionTieneAlMenosUnaRazon() {
        for (franja in Franja.entries) for (actividad in Actividad.entries) {
            val recs = recomendar(franja = franja, actividad = actividad).recomendaciones
            assertTrue(recs.all { it.razones.isNotEmpty() })
        }
    }
}
