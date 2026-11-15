package com.grupo3.compras.domain

import com.grupo3.compras.data.Catalogo
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecomendadorTest {
    private val motor = Recomendador()
    private val ahora = 1_800_000_000_000L
    private val diaMs = 86_400_000.0

    // Contextos de prueba
    private val semanaManana = Contexto(TipoDia.ENTRE_SEMANA, Momento.MANANA)
    private val semanaNoche = Contexto(TipoDia.ENTRE_SEMANA, Momento.NOCHE)
    private val finDeSemanaTarde = Contexto(TipoDia.FIN_DE_SEMANA, Momento.TARDE)

    private fun accion(
        productoId: String?,
        categoria: Categoria,
        tipo: TipoInteraccion,
        diasAtras: Double = 0.0,
        contexto: Contexto = semanaManana
    ) = Interaccion(productoId, categoria, tipo, ahora - (diasAtras * diaMs).toLong(), contexto)

    private fun recomendar(
        franja: Franja,
        historial: List<Interaccion> = emptyList(),
        favoritos: Set<String> = emptySet(),
        mostrados: List<String> = emptyList(),
        contexto: Contexto = semanaManana
    ) = motor.recomendar(Catalogo.productos, historial, favoritos, franja, contexto, ahora, mostrados)

    private fun ids(recs: List<Recomendacion>) = recs.map { it.producto.id }

    // ------------------------------------------------------------ contexto horario

    @Test
    fun enHorarioLaboralSeRecomiendaOficina() {
        val recs = recomendar(Franja.LABORAL)
        assertEquals(3, recs.size)
        assertEquals(Categoria.OFICINA, recs[0].producto.categoria)
        assertEquals(Categoria.OFICINA, recs[1].producto.categoria)
        assertTrue(recs[2].producto.categoria != Categoria.OFICINA)
    }

    @Test
    fun enFinDeSemanaSeRecomiendaEntretenimiento() {
        val recs = recomendar(Franja.FIN_DE_SEMANA)
        assertEquals(3, recs.size)
        assertEquals(Categoria.ENTRETENIMIENTO, recs[0].producto.categoria)
        assertEquals(Categoria.ENTRETENIMIENTO, recs[1].producto.categoria)
        assertTrue(recs[2].producto.categoria != Categoria.ENTRETENIMIENTO)
    }

    @Test
    fun fueraDeHorarioNuncaPasaDeDosPorCategoria() {
        val recs = recomendar(Franja.OTRO)
        assertEquals(3, recs.size)
        Categoria.entries.forEach { c ->
            assertTrue(recs.count { it.producto.categoria == c } <= Recomendador.MAX_POR_CATEGORIA)
        }
    }

    @Test
    fun laFranjaSeDetectaBienEnLosBordes() {
        // 2026-11-14 es sábado, 2026-11-16 es lunes
        assertEquals(Franja.FIN_DE_SEMANA, Franja.de(LocalDateTime.of(2026, 11, 14, 10, 0)))
        assertEquals(Franja.FIN_DE_SEMANA, Franja.de(LocalDateTime.of(2026, 11, 15, 23, 59)))
        assertEquals(Franja.OTRO, Franja.de(LocalDateTime.of(2026, 11, 16, 8, 59)))
        assertEquals(Franja.LABORAL, Franja.de(LocalDateTime.of(2026, 11, 16, 9, 0)))
        assertEquals(Franja.LABORAL, Franja.de(LocalDateTime.of(2026, 11, 20, 17, 59)))
        assertEquals(Franja.OTRO, Franja.de(LocalDateTime.of(2026, 11, 20, 18, 0)))
        assertEquals(Franja.OTRO, Franja.de(LocalDateTime.of(2026, 11, 18, 21, 0)))
    }

    // ------------------------------------------------------------ aprendizaje

    @Test
    fun losPesosSonBusqueda1Clic2Favorito3() {
        assertEquals(1, TipoInteraccion.BUSQUEDA.peso)
        assertEquals(2, TipoInteraccion.CLIC.peso)
        assertEquals(3, TipoInteraccion.FAVORITO.peso)
    }

    @Test
    fun aprendeDeLosClicsYLoMuestraEnElTercerLugar() {
        val historial = listOf(
            accion("te3", Categoria.TECNOLOGIA, TipoInteraccion.CLIC),
            accion("te3", Categoria.TECNOLOGIA, TipoInteraccion.CLIC),
            accion("te4", Categoria.TECNOLOGIA, TipoInteraccion.CLIC)
        )
        val laboral = recomendar(Franja.LABORAL, historial)
        assertEquals(Categoria.OFICINA, laboral[1].producto.categoria)
        assertEquals("te3", laboral[2].producto.id)

        val libre = recomendar(Franja.OTRO, historial)
        assertEquals("te3", libre[0].producto.id)
        assertEquals("te4", libre[1].producto.id)
    }

    @Test
    fun aprendeDeLasBusquedasAunqueNoHayaProducto() {
        val historial = List(3) { accion(null, Categoria.HOGAR, TipoInteraccion.BUSQUEDA) }
        val recs = recomendar(Franja.OTRO, historial)
        assertEquals(Categoria.HOGAR, recs[0].producto.categoria)
        assertEquals(Categoria.HOGAR, recs[1].producto.categoria)
        assertTrue(recs[0].motivo.contains("Hogar"))
    }

    @Test
    fun unFavoritoPesaMasQueUnClic() {
        val conFavorito = listOf(accion("ho1", Categoria.HOGAR, TipoInteraccion.FAVORITO))
        val conClic = listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC))
        val puntos = motor.puntosPorCategoria(conFavorito + conClic, ahora)
        assertTrue(puntos.getValue(Categoria.HOGAR) > puntos.getValue(Categoria.TECNOLOGIA))
    }

    @Test
    fun lasAccionesRecientesPesanMasQueLasViejas() {
        val historial = List(5) { accion("ho1", Categoria.HOGAR, TipoInteraccion.FAVORITO, diasAtras = 30.0) } +
            List(2) { accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC) }
        val puntos = motor.puntosPorCategoria(historial, ahora)
        assertTrue(puntos.getValue(Categoria.TECNOLOGIA) > puntos.getValue(Categoria.HOGAR))
    }

    @Test
    fun conElTiempoUnPuntoVaLaMitadCadaSemana() {
        val hoy = motor.puntosPorCategoria(listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC)), ahora)
        val enUnaSemana = motor.puntosPorCategoria(
            listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.CLIC, diasAtras = 7.0)), ahora
        )
        assertEquals(2.0, hoy.getValue(Categoria.TECNOLOGIA), 1e-9)
        assertEquals(1.0, enUnaSemana.getValue(Categoria.TECNOLOGIA), 1e-9)
    }

    // ------------------------------------------------------------ favoritos

    @Test
    fun losFavoritosNoSeVuelvenARecomendar() {
        val favoritos = setOf("of1", "of2", "te2")
        for (franja in Franja.entries) {
            val recs = recomendar(franja, favoritos = favoritos)
            assertTrue(recs.none { it.producto.id in favoritos })
        }
    }

    @Test
    fun siQuedanPocosDeLaCategoriaSigueHabiendoTresRecomendaciones() {
        val favoritos = Catalogo.productos
            .filter { it.categoria == Categoria.OFICINA && it.id != "of6" }
            .map { it.id }
            .toSet()
        val recs = recomendar(Franja.LABORAL, favoritos = favoritos)
        assertEquals(3, recs.size)
        assertEquals("of6", recs[0].producto.id)
        assertEquals(3, ids(recs).toSet().size)
    }

    // ------------------------------------------------------------ variedad

    @Test
    fun alPedirOtrasNoSeRepitenLosProductos() {
        for (franja in Franja.entries) {
            val primera = ids(recomendar(franja))
            val segunda = ids(recomendar(franja, mostrados = primera))
            assertTrue(
                "$franja repitió ${primera.intersect(segunda.toSet())}",
                primera.intersect(segunda.toSet()).isEmpty()
            )
        }
    }

    @Test
    fun alRotarSeVenTodosLosProductosDeLaCategoriaDelContexto() {
        for ((franja, categoria) in listOf(
            Franja.LABORAL to Categoria.OFICINA,
            Franja.FIN_DE_SEMANA to Categoria.ENTRETENIMIENTO
        )) {
            var recientes = emptyList<String>()
            val vistos = mutableSetOf<String>()
            var anterior = emptyList<String>()
            repeat(6) {
                val recs = recomendar(franja, mostrados = recientes)
                val actuales = ids(recs)
                assertTrue("lote repetido en $franja", actuales != anterior)
                assertEquals(categoria, recs[0].producto.categoria)
                assertEquals(categoria, recs[1].producto.categoria)
                vistos += actuales
                anterior = actuales
                recientes = (actuales + recientes).take(18) // igual que "Ver otras" en la app (6 lotes de 3)
            }
            val delContexto = Catalogo.productos.filter { it.categoria == categoria }.map { it.id }.toSet()
            assertTrue("faltaron ${delContexto - vistos}", vistos.containsAll(delContexto))
        }
    }

    // ------------------------------------------------------------ Naive Bayes

    @Test
    fun naiveBayesSinHistorialEsUniformeYSumaUno() {
        val p = motor.probabilidadesPorCategoria(emptyList(), semanaManana, ahora)
        assertEquals(1.0, p.values.sum(), 1e-9)
        p.values.forEach { assertEquals(0.25, it, 1e-9) }
    }

    @Test
    fun naiveBayesAprendeQueCategoriaVaConCadaMomento() {
        val historial = List(4) { accion("of1", Categoria.OFICINA, TipoInteraccion.CLIC, contexto = semanaManana) } +
            List(4) { accion("en1", Categoria.ENTRETENIMIENTO, TipoInteraccion.CLIC, contexto = finDeSemanaTarde) }
        val enLaSemana = motor.probabilidadesPorCategoria(historial, semanaManana, ahora)
        val enElFinDeSemana = motor.probabilidadesPorCategoria(historial, finDeSemanaTarde, ahora)
        assertTrue(enLaSemana.getValue(Categoria.OFICINA) > enLaSemana.getValue(Categoria.ENTRETENIMIENTO))
        assertTrue(enElFinDeSemana.getValue(Categoria.ENTRETENIMIENTO) > enElFinDeSemana.getValue(Categoria.OFICINA))
        assertEquals(1.0, enLaSemana.values.sum(), 1e-9)
        assertTrue(enLaSemana.values.all { it > 0.0 })
    }

    @Test
    fun naiveBayesDaMasPesoAUnFavoritoQueAUnaBusqueda() {
        val historial = listOf(
            accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.FAVORITO),
            accion(null, Categoria.HOGAR, TipoInteraccion.BUSQUEDA)
        )
        val p = motor.probabilidadesPorCategoria(historial, semanaManana, ahora)
        assertTrue(p.getValue(Categoria.TECNOLOGIA) > p.getValue(Categoria.HOGAR))
    }

    @Test
    fun naiveBayesOlvidaLoViejo() {
        val historial = List(10) { accion("of1", Categoria.OFICINA, TipoInteraccion.CLIC, diasAtras = 60.0) } +
            accion("ho1", Categoria.HOGAR, TipoInteraccion.CLIC)
        val p = motor.probabilidadesPorCategoria(historial, semanaManana, ahora)
        assertTrue(p.getValue(Categoria.HOGAR) > p.getValue(Categoria.OFICINA))
    }

    @Test
    fun elMomentoCambiaLaRecomendacionFueraDeHorario() {
        val historial = List(3) { accion("te3", Categoria.TECNOLOGIA, TipoInteraccion.CLIC, contexto = semanaNoche) } +
            List(3) { accion("en2", Categoria.ENTRETENIMIENTO, TipoInteraccion.CLIC, contexto = semanaManana) }
        val enLaNoche = recomendar(Franja.OTRO, historial, contexto = semanaNoche)
        val enLaManana = recomendar(Franja.OTRO, historial, contexto = semanaManana)
        assertEquals(Categoria.TECNOLOGIA, enLaNoche[0].producto.categoria)
        assertEquals(Categoria.ENTRETENIMIENTO, enLaManana[0].producto.categoria)
    }

    @Test
    fun contextoContradictorioDaOficinaPeroRespetaElGusto() {
        // Le gusta el entretenimiento, pero está en horario laboral.
        val historial = List(4) { accion("en1", Categoria.ENTRETENIMIENTO, TipoInteraccion.CLIC) }
        val recs = recomendar(Franja.LABORAL, historial)
        assertEquals(Categoria.OFICINA, recs[0].producto.categoria)
        assertEquals(Categoria.OFICINA, recs[1].producto.categoria)
        assertEquals(Categoria.ENTRETENIMIENTO, recs[2].producto.categoria)
    }

    @Test
    fun elMotivoMuestraLaProbabilidadDeNaiveBayes() {
        val historial = List(3) { accion("te3", Categoria.TECNOLOGIA, TipoInteraccion.CLIC) }
        val recs = recomendar(Franja.OTRO, historial)
        assertTrue(recs[0].motivo.contains("%"))
        assertTrue(recs[0].motivo.contains("Tecnología"))
    }

    @Test
    fun siLaCategoriaNoEncajaConElMomentoPeroYaLoVisteSoloDiceQueLoViste() {
        val historial = List(6) { accion("of1", Categoria.OFICINA, TipoInteraccion.CLIC, contexto = finDeSemanaTarde) } +
            List(6) { accion("en1", Categoria.ENTRETENIMIENTO, TipoInteraccion.CLIC, contexto = semanaManana) }
        val recs = recomendar(Franja.OTRO, historial, contexto = semanaManana)
        assertEquals("Lo viste antes", recs.first { it.producto.id == "of1" }.motivo)
    }

    @Test
    fun elContextoSeArmaConDiaYMomento() {
        assertEquals(Contexto(TipoDia.FIN_DE_SEMANA, Momento.MANANA), Contexto.de(LocalDateTime.of(2026, 11, 14, 10, 0)))
        assertEquals(Contexto(TipoDia.ENTRE_SEMANA, Momento.TARDE), Contexto.de(LocalDateTime.of(2026, 11, 16, 12, 0)))
        assertEquals(Momento.NOCHE, Contexto.de(LocalDateTime.of(2026, 11, 16, 4, 59)).momento)
        assertEquals(Momento.MANANA, Contexto.de(LocalDateTime.of(2026, 11, 16, 5, 0)).momento)
        assertEquals(Momento.TARDE, Contexto.de(LocalDateTime.of(2026, 11, 16, 17, 59)).momento)
        assertEquals(Momento.NOCHE, Contexto.de(LocalDateTime.of(2026, 11, 16, 18, 0)).momento)
    }

    // ------------------------------------------------------------ parecidos (búsquedas y favoritos al instante)

    @Test
    fun laMejorCoincidenciaDeUnaBusquedaEsElNombreMasPopular() {
        assertEquals("te1", Catalogo.mejorCoincidencia("laptop")?.id)
        assertEquals("te3", Catalogo.mejorCoincidencia("teclado")?.id)
        assertEquals("of1", Catalogo.mejorCoincidencia("silla")?.id)
        assertEquals(null, Catalogo.mejorCoincidencia("zzzz"))
        assertEquals(null, Catalogo.mejorCoincidencia("  "))
    }

    @Test
    fun sinHistorialNoHayRelacionados() {
        val r = motor.relacionados(Catalogo.productos, emptyList(), emptySet(), emptySet(), ahora)
        assertTrue(r.isEmpty())
    }

    @Test
    fun unFavoritoHaceAparecerProductosParecidos() {
        val historial = listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.FAVORITO))
        val r = motor.relacionados(Catalogo.productos, historial, setOf("te1"), emptySet(), ahora)
        assertEquals(3, r.size)
        assertTrue(r.none { it.producto.id == "te1" })
        assertTrue(r.all { it.motivo == "Parecido a tu favorito: Laptop 14\"" })
        // La laptop comparte etiquetas con monitor, mouse y teclado.
        assertTrue(ids(r).all { it in setOf("te7", "te4", "te3", "of11", "te9", "te5", "te2", "te8") })
    }

    @Test
    fun unaBusquedaTambienGeneraRelacionados() {
        val historial = listOf(accion("te3", Categoria.TECNOLOGIA, TipoInteraccion.BUSQUEDA))
        val r = motor.relacionados(Catalogo.productos, historial, emptySet(), emptySet(), ahora)
        assertTrue(r.isNotEmpty())
        assertTrue(r.all { it.motivo == "Parecido a lo que buscaste: Teclado mecánico" })
        assertTrue(r.none { it.producto.id == "te3" })
    }

    @Test
    fun losRelacionadosNoRepitenLoPrincipalNiLosFavoritos() {
        val historial = listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.FAVORITO))
        val principales = recomendar(Franja.FIN_DE_SEMANA, historial, setOf("te1"))
        val r = motor.relacionados(
            Catalogo.productos, historial, setOf("te1"), principales.map { it.producto.id }.toSet(), ahora
        )
        assertTrue(r.none { it.producto.id in principales.map { p -> p.producto.id } })
        assertTrue(r.none { it.producto.id == "te1" })
    }

    @Test
    fun buscarOGuardarSeNotaEnLaListaPrincipalAunEnFinDeSemana() {
        // Sábado: 2 lugares son de entretenimiento, pero el tercero responde a lo que guardó.
        val historial = listOf(accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.FAVORITO, contexto = finDeSemanaTarde))
        val recs = recomendar(Franja.FIN_DE_SEMANA, historial, setOf("te1"), contexto = finDeSemanaTarde)
        assertEquals(Categoria.ENTRETENIMIENTO, recs[0].producto.categoria)
        assertEquals(Categoria.ENTRETENIMIENTO, recs[1].producto.categoria)
        assertTrue(recs[2].motivo.contains("Laptop"))
    }

    @Test
    fun cadaCosaQueHaceElUsuarioTieneSuLugarEnLosRelacionados() {
        val historial = listOf(
            accion("te1", Categoria.TECNOLOGIA, TipoInteraccion.FAVORITO),
            accion("ho1", Categoria.HOGAR, TipoInteraccion.BUSQUEDA),
            accion("en5", Categoria.ENTRETENIMIENTO, TipoInteraccion.FAVORITO)
        )
        val r = motor.relacionados(Catalogo.productos, historial, setOf("te1", "en5"), emptySet(), ahora)
        assertEquals(3, r.size)
        val motivos = r.map { it.motivo }.toSet()
        assertEquals(
            setOf(
                "Parecido a tu favorito: Laptop 14\"",
                "Parecido a lo que buscaste: Cafetera eléctrica",
                "Parecido a tu favorito: Guitarra acústica"
            ),
            motivos
        )
    }

    @Test
    fun lasEtiquetasRelacionanCategoriasDistintas() {
        // Un favorito de cocina sugiere más de cocina, no de oficina.
        val historial = listOf(accion("ho1", Categoria.HOGAR, TipoInteraccion.FAVORITO))
        val r = motor.relacionados(Catalogo.productos, historial, setOf("ho1"), emptySet(), ahora)
        assertTrue(r.all { it.producto.etiquetas.contains("cocina") })
    }

    // ------------------------------------------------------------ general

    @Test
    fun todaRecomendacionTieneMotivoYNoSeRepite() {
        for (franja in Franja.entries) {
            val recs = recomendar(franja)
            assertTrue(recs.all { it.motivo.isNotBlank() })
            assertEquals(recs.size, ids(recs).toSet().size)
        }
    }

    @Test
    fun conCatalogoVacioNoFalla() {
        val recs = motor.recomendar(emptyList(), emptyList(), emptySet(), Franja.LABORAL, semanaManana, ahora)
        assertTrue(recs.isEmpty())
    }

    // ------------------------------------------------------------ catálogo

    @Test
    fun elCatalogoEstaBienArmado() {
        val todos = Catalogo.productos
        assertEquals(todos.size, todos.map { it.id }.toSet().size)
        Categoria.entries.forEach { c -> assertEquals(12, todos.count { it.categoria == c }) }
        assertTrue(todos.all { it.etiquetas.isNotEmpty() })
        assertTrue(todos.all { it.precio > 0 && it.nombre.isNotBlank() })
    }

    @Test
    fun laBusquedaIgnoraTildesYMayusculas() {
        assertEquals(setOf("te3", "en9"), Catalogo.buscar("TECLADO").map { it.id }.toSet())
        assertEquals(12, Catalogo.buscar("tecnologia").size)
        assertTrue(Catalogo.buscar("música").any { it.id == "en5" })
        assertTrue(Catalogo.buscar("Silla").any { it.id == "of1" })
        assertEquals(Catalogo.productos.size, Catalogo.buscar("").size)
        assertTrue(Catalogo.buscar("zzzz").isEmpty())
        assertFalse(Catalogo.buscar("oficina", Categoria.HOGAR).isNotEmpty())
    }
}
