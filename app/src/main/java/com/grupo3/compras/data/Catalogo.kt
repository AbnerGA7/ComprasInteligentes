package com.grupo3.compras.data

import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Producto
import java.text.Normalizer
import java.util.Locale

/** Catálogo local de ejemplo (27 productos). Los precios están en soles. */
object Catalogo {
    val productos: List<Producto> = listOf(
        // ---- Oficina ----
        Producto("of1", "Set de útiles de oficina", Categoria.OFICINA, 39.90,
            "Grapadora, perforador, tijeras y clips en un solo kit.", "📎", popularidad = 62),
        Producto("of2", "Silla ergonómica", Categoria.OFICINA, 389.00,
            "Respaldo lumbar ajustable y malla transpirable para jornadas largas.", "🪑", 459.00, 85),
        Producto("of3", "Impresora multifuncional", Categoria.OFICINA, 459.00,
            "Imprime, escanea y copia con conexión wifi.", "🖨️", popularidad = 70),
        Producto("of4", "Agenda ejecutiva 2027", Categoria.OFICINA, 29.90,
            "Planificador semanal con tapa dura y marcador.", "📒", popularidad = 55),
        Producto("of5", "Pack de 12 lapiceros gel", Categoria.OFICINA, 24.50,
            "Tinta de secado rápido, trazo fino y suave.", "🖊️", popularidad = 48),
        Producto("of6", "Archivador de escritorio", Categoria.OFICINA, 54.90,
            "Organiza documentos con 5 niveles y estructura metálica.", "🗄️", popularidad = 50),

        // ---- Entretenimiento ----
        Producto("en1", "Consola de videojuegos", Categoria.ENTRETENIMIENTO, 1899.00,
            "Gráficos 4K, 1 TB de almacenamiento y un control incluido.", "🎮", popularidad = 92),
        Producto("en2", "Audífonos inalámbricos", Categoria.ENTRETENIMIENTO, 249.00,
            "Cancelación de ruido y hasta 30 horas de batería.", "🎧", 329.00, 88),
        Producto("en3", "Juego de mesa Catán", Categoria.ENTRETENIMIENTO, 189.00,
            "Estrategia y comercio para 3 a 4 jugadores.", "🎲", popularidad = 66),
        Producto("en4", "Proyector portátil", Categoria.ENTRETENIMIENTO, 599.00,
            "Cine en casa con resolución Full HD y parlante integrado.", "🎬", 749.00, 74),
        Producto("en5", "Guitarra acústica", Categoria.ENTRETENIMIENTO, 329.00,
            "Cuerpo de madera, ideal para principiantes. Incluye funda.", "🎸", popularidad = 58),
        Producto("en6", "Rompecabezas 1000 piezas", Categoria.ENTRETENIMIENTO, 59.90,
            "Paisaje montañoso para armar en familia.", "🧩", popularidad = 45),

        // ---- Tecnología ----
        Producto("te1", "Laptop 14\"", Categoria.TECNOLOGIA, 2799.00,
            "16 GB de RAM, SSD de 512 GB y pantalla antirreflejo.", "💻", 3199.00, 90),
        Producto("te2", "Smartphone 128 GB", Categoria.TECNOLOGIA, 1199.00,
            "Cámara dual de 50 MP y batería de 5000 mAh.", "📱", popularidad = 94),
        Producto("te3", "Teclado mecánico", Categoria.TECNOLOGIA, 219.00,
            "Switches rojos, retroiluminación y diseño compacto.", "⌨️", popularidad = 72),
        Producto("te4", "Mouse inalámbrico", Categoria.TECNOLOGIA, 69.00,
            "Sensor de precisión y batería de hasta 12 meses.", "🖱️", 89.00, 64),
        Producto("te5", "Cargador rápido 65 W", Categoria.TECNOLOGIA, 89.00,
            "Carga laptop y celular con un solo puerto USB-C.", "🔌", popularidad = 60),

        // ---- Hogar ----
        Producto("ho1", "Cafetera eléctrica", Categoria.HOGAR, 159.00,
            "Prepara hasta 12 tazas, con jarra de vidrio y filtro lavable.", "☕", 199.00, 68),
        Producto("ho2", "Lámpara LED de escritorio", Categoria.HOGAR, 79.00,
            "Tres tonos de luz y brazo flexible.", "💡", popularidad = 56),
        Producto("ho3", "Sartén antiadherente", Categoria.HOGAR, 69.00,
            "28 cm, apta para todo tipo de cocinas.", "🍳", popularidad = 52),
        Producto("ho4", "Organizador multiuso", Categoria.HOGAR, 45.00,
            "Cajas apilables para ropa, juguetes o herramientas.", "🧺", popularidad = 44),
        Producto("ho5", "Set de velas aromáticas", Categoria.HOGAR, 35.00,
            "Tres fragancias relajantes: lavanda, vainilla y cítricos.", "🕯️", popularidad = 47),

        // ---- Deporte ----
        Producto("de1", "Zapatillas running", Categoria.DEPORTE, 269.00,
            "Suela amortiguada y malla ligera para correr a diario.", "👟", 339.00, 80),
        Producto("de2", "Mat de yoga", Categoria.DEPORTE, 59.00,
            "Antideslizante de 6 mm con correa para transportar.", "🧘", popularidad = 54),
        Producto("de3", "Mancuernas 10 kg", Categoria.DEPORTE, 129.00,
            "Par de mancuernas con recubrimiento de goma.", "🏋️", popularidad = 59),
        Producto("de4", "Casco de ciclismo", Categoria.DEPORTE, 119.00,
            "Ventilado, liviano y con ajuste fácil.", "🚴", popularidad = 46),
        Producto("de5", "Botella térmica 750 ml", Categoria.DEPORTE, 49.00,
            "Mantiene el frío por 24 horas y el calor por 12.", "💧", 65.00, 61)
    )

    private val indice: Map<String, Producto> = productos.associateBy { it.id }

    fun porId(id: String?): Producto? = if (id == null) null else indice[id]

    fun buscar(consulta: String, categoria: Categoria? = null): List<Producto> {
        val q = normalizar(consulta)
        return productos.filter { p ->
            (categoria == null || p.categoria == categoria) &&
                (q.isEmpty() ||
                    normalizar(p.nombre).contains(q) ||
                    normalizar(p.descripcion).contains(q) ||
                    normalizar(p.categoria.etiqueta).contains(q))
        }
    }

    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .trim()
}
