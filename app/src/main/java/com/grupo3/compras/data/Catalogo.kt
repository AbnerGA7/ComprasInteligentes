package com.grupo3.compras.data

import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Producto
import java.text.Normalizer
import java.util.Locale

/** Catálogo local de ejemplo: 24 productos, precios en soles. */
object Catalogo {
    val productos: List<Producto> = listOf(
        // Oficina
        Producto("of1", "Silla ergonómica", Categoria.OFICINA, 389.00,
            "Respaldo lumbar ajustable y malla transpirable.", "🪑", 85),
        Producto("of2", "Impresora multifuncional", Categoria.OFICINA, 459.00,
            "Imprime, escanea y copia con conexión wifi.", "🖨️", 70),
        Producto("of3", "Set de útiles de oficina", Categoria.OFICINA, 39.90,
            "Grapadora, perforador, tijeras y clips.", "📎", 62),
        Producto("of4", "Agenda ejecutiva 2027", Categoria.OFICINA, 29.90,
            "Planificador semanal con tapa dura.", "📒", 55),
        Producto("of5", "Archivador de escritorio", Categoria.OFICINA, 54.90,
            "Organiza documentos en 5 niveles.", "🗄️", 50),
        Producto("of6", "Pack de 12 lapiceros gel", Categoria.OFICINA, 24.50,
            "Tinta de secado rápido y trazo fino.", "🖊️", 48),

        // Entretenimiento
        Producto("en1", "Consola de videojuegos", Categoria.ENTRETENIMIENTO, 1899.00,
            "Gráficos 4K y 1 TB de almacenamiento.", "🎮", 92),
        Producto("en2", "Audífonos inalámbricos", Categoria.ENTRETENIMIENTO, 249.00,
            "Cancelación de ruido y 30 horas de batería.", "🎧", 88),
        Producto("en3", "Juego de mesa Catán", Categoria.ENTRETENIMIENTO, 189.00,
            "Estrategia y comercio para 3 a 4 jugadores.", "🎲", 66),
        Producto("en4", "Proyector portátil", Categoria.ENTRETENIMIENTO, 599.00,
            "Cine en casa en Full HD con parlante integrado.", "🎬", 74),
        Producto("en5", "Guitarra acústica", Categoria.ENTRETENIMIENTO, 329.00,
            "Cuerpo de madera, ideal para principiantes.", "🎸", 58),
        Producto("en6", "Rompecabezas 1000 piezas", Categoria.ENTRETENIMIENTO, 59.90,
            "Paisaje de montaña para armar en familia.", "🧩", 45),

        // Tecnología
        Producto("te1", "Laptop 14\"", Categoria.TECNOLOGIA, 2799.00,
            "16 GB de RAM y SSD de 512 GB.", "💻", 90),
        Producto("te2", "Smartphone 128 GB", Categoria.TECNOLOGIA, 1199.00,
            "Cámara de 50 MP y batería de 5000 mAh.", "📱", 94),
        Producto("te3", "Teclado mecánico", Categoria.TECNOLOGIA, 219.00,
            "Switches rojos y retroiluminación.", "⌨️", 72),
        Producto("te4", "Mouse inalámbrico", Categoria.TECNOLOGIA, 69.00,
            "Sensor de precisión, batería de 12 meses.", "🖱️", 64),
        Producto("te5", "Cargador rápido 65 W", Categoria.TECNOLOGIA, 89.00,
            "Carga laptop y celular por USB-C.", "🔌", 60),
        Producto("te6", "Disco duro externo 1 TB", Categoria.TECNOLOGIA, 229.00,
            "Respaldo portátil con conexión USB 3.0.", "💾", 57),

        // Hogar
        Producto("ho1", "Cafetera eléctrica", Categoria.HOGAR, 159.00,
            "Prepara hasta 12 tazas con filtro lavable.", "☕", 68),
        Producto("ho2", "Lámpara LED de escritorio", Categoria.HOGAR, 79.00,
            "Tres tonos de luz y brazo flexible.", "💡", 56),
        Producto("ho3", "Sartén antiadherente", Categoria.HOGAR, 69.00,
            "28 cm, apta para todo tipo de cocinas.", "🍳", 52),
        Producto("ho4", "Organizador multiuso", Categoria.HOGAR, 45.00,
            "Cajas apilables para ropa o herramientas.", "🧺", 44),
        Producto("ho5", "Set de velas aromáticas", Categoria.HOGAR, 35.00,
            "Lavanda, vainilla y cítricos.", "🕯️", 47),
        Producto("ho6", "Hervidor eléctrico", Categoria.HOGAR, 99.00,
            "1.7 litros con apagado automático.", "🫖", 50)
    )

    private val indice: Map<String, Producto> = productos.associateBy { it.id }

    fun porId(id: String?): Producto? = if (id == null) null else indice[id]

    /** Busca en nombre, descripción y categoría, sin importar mayúsculas ni tildes. */
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
