package com.grupo3.compras.data

import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Producto
import java.text.Normalizer
import java.util.Locale

/**
 * Catálogo local de ejemplo: 48 productos (12 por categoría), precios en soles.
 * Las etiquetas relacionan productos entre sí, incluso de categorías distintas
 * (por ejemplo, una laptop y un monitor comparten "computo" y "escritorio").
 */
object Catalogo {
    val productos: List<Producto> = listOf(
        // ---------------- Oficina ----------------
        Producto("of1", "Silla ergonómica", Categoria.OFICINA, 389.00,
            "Respaldo lumbar ajustable y malla transpirable.", "💺", 85, setOf("ergonomia", "escritorio")),
        Producto("of2", "Impresora multifuncional", Categoria.OFICINA, 459.00,
            "Imprime, escanea y copia con conexión wifi.", "🖨️", 70, setOf("computo", "papeleria", "escritorio")),
        Producto("of3", "Set de útiles de oficina", Categoria.OFICINA, 39.90,
            "Grapadora, perforador, tijeras y clips.", "📎", 62, setOf("papeleria", "escritorio", "ordenar")),
        Producto("of4", "Agenda ejecutiva 2027", Categoria.OFICINA, 29.90,
            "Planificador semanal con tapa dura.", "📒", 55, setOf("papeleria", "ordenar")),
        Producto("of5", "Archivador de escritorio", Categoria.OFICINA, 54.90,
            "Organiza documentos en 5 niveles.", "🗄️", 50, setOf("ordenar", "escritorio", "papeleria")),
        Producto("of6", "Pack de 12 lapiceros gel", Categoria.OFICINA, 24.50,
            "Tinta de secado rápido y trazo fino.", "🖊️", 48, setOf("papeleria", "escritorio")),
        Producto("of7", "Calculadora científica", Categoria.OFICINA, 59.90,
            "Pantalla de 2 líneas y más de 240 funciones.", "🔢", 52, setOf("papeleria", "escritorio")),
        Producto("of8", "Resma de papel bond A4", Categoria.OFICINA, 19.90,
            "500 hojas de 75 g para impresora.", "📄", 58, setOf("papeleria", "computo")),
        Producto("of9", "Pizarra acrílica con marcadores", Categoria.OFICINA, 89.00,
            "60 x 90 cm, ideal para reuniones.", "📝", 46, setOf("papeleria", "ordenar")),
        Producto("of10", "Reposapiés ergonómico", Categoria.OFICINA, 79.00,
            "Inclinación regulable para trabajar sentado.", "🦶", 42, setOf("ergonomia", "escritorio")),
        Producto("of11", "Soporte para laptop", Categoria.OFICINA, 69.00,
            "Aluminio plegable con altura ajustable.", "📐", 55, setOf("computo", "ergonomia", "escritorio")),
        Producto("of12", "Trituradora de papel", Categoria.OFICINA, 249.00,
            "Destruye hojas y tarjetas de forma segura.", "🗑️", 40, setOf("escritorio", "ordenar")),

        // ---------------- Entretenimiento ----------------
        Producto("en1", "Consola de videojuegos", Categoria.ENTRETENIMIENTO, 1899.00,
            "Gráficos 4K y 1 TB de almacenamiento.", "🎮", 92, setOf("juegos", "cine")),
        Producto("en2", "Audífonos inalámbricos", Categoria.ENTRETENIMIENTO, 249.00,
            "Cancelación de ruido y 30 horas de batería.", "🎧", 88, setOf("audio", "musica", "movil")),
        Producto("en3", "Juego de mesa Catán", Categoria.ENTRETENIMIENTO, 189.00,
            "Estrategia y comercio para 3 a 4 jugadores.", "🎲", 66, setOf("juegos", "familia")),
        Producto("en4", "Proyector portátil", Categoria.ENTRETENIMIENTO, 599.00,
            "Cine en casa en Full HD con parlante integrado.", "🎬", 74, setOf("cine", "familia")),
        Producto("en5", "Guitarra acústica", Categoria.ENTRETENIMIENTO, 329.00,
            "Cuerpo de madera, ideal para principiantes.", "🎸", 58, setOf("musica")),
        Producto("en6", "Rompecabezas 1000 piezas", Categoria.ENTRETENIMIENTO, 59.90,
            "Paisaje de montaña para armar en familia.", "🧩", 45, setOf("juegos", "familia", "relax")),
        Producto("en7", "Parlante bluetooth", Categoria.ENTRETENIMIENTO, 179.00,
            "Sonido de 360° resistente al agua.", "🔊", 80, setOf("audio", "musica", "movil")),
        Producto("en8", "Control inalámbrico para consola", Categoria.ENTRETENIMIENTO, 289.00,
            "Vibración y batería de 40 horas.", "🕹️", 76, setOf("juegos")),
        Producto("en9", "Teclado de piano de 61 teclas", Categoria.ENTRETENIMIENTO, 429.00,
            "Teclas sensibles y 100 ritmos incluidos.", "🎹", 54, setOf("musica")),
        Producto("en10", "Novela de aventuras", Categoria.ENTRETENIMIENTO, 49.90,
            "Best seller de 400 páginas en tapa blanda.", "📚", 60, setOf("lectura", "relax")),
        Producto("en11", "Set de cartas y dados", Categoria.ENTRETENIMIENTO, 29.90,
            "Para juntarse con amigos o en familia.", "🃏", 49, setOf("juegos", "familia")),
        Producto("en12", "Kit de pintura al óleo", Categoria.ENTRETENIMIENTO, 79.00,
            "Pinceles, lienzos y 12 colores.", "🎨", 47, setOf("relax", "decoracion")),

        // ---------------- Tecnología ----------------
        Producto("te1", "Laptop 14\"", Categoria.TECNOLOGIA, 2799.00,
            "16 GB de RAM y SSD de 512 GB.", "💻", 90, setOf("computo", "escritorio", "movil")),
        Producto("te2", "Smartphone 128 GB", Categoria.TECNOLOGIA, 1199.00,
            "Cámara de 50 MP y batería de 5000 mAh.", "📱", 94, setOf("movil")),
        Producto("te3", "Teclado mecánico", Categoria.TECNOLOGIA, 219.00,
            "Switches rojos y retroiluminación.", "⌨️", 72, setOf("computo", "escritorio", "juegos")),
        Producto("te4", "Mouse inalámbrico", Categoria.TECNOLOGIA, 69.00,
            "Sensor de precisión, batería de 12 meses.", "🖱️", 64, setOf("computo", "escritorio")),
        Producto("te5", "Cargador rápido 65 W", Categoria.TECNOLOGIA, 89.00,
            "Carga laptop y celular por USB-C.", "🔌", 60, setOf("movil", "computo")),
        Producto("te6", "Disco duro externo 1 TB", Categoria.TECNOLOGIA, 229.00,
            "Respaldo portátil con conexión USB 3.0.", "💾", 57, setOf("computo")),
        Producto("te7", "Monitor 24\" Full HD", Categoria.TECNOLOGIA, 529.00,
            "Panel IPS de 75 Hz con entrada HDMI.", "🖥️", 78, setOf("computo", "escritorio")),
        Producto("te8", "Tablet 10\"", Categoria.TECNOLOGIA, 899.00,
            "Pantalla de 2K, ideal para leer y ver series.", "📲", 70, setOf("movil", "lectura")),
        Producto("te9", "Cámara web Full HD", Categoria.TECNOLOGIA, 129.00,
            "Micrófono integrado para videollamadas.", "📷", 58, setOf("computo", "escritorio")),
        Producto("te10", "Batería portátil 20000 mAh", Categoria.TECNOLOGIA, 119.00,
            "Carga rápida para dos dispositivos a la vez.", "🔋", 66, setOf("movil")),
        Producto("te11", "Reloj inteligente", Categoria.TECNOLOGIA, 349.00,
            "Mide pasos, sueño y recibe notificaciones.", "⌚", 72, setOf("movil")),
        Producto("te12", "Router wifi 6", Categoria.TECNOLOGIA, 199.00,
            "Cobertura para toda la casa y 30 equipos.", "📡", 63, setOf("computo")),

        // ---------------- Hogar ----------------
        Producto("ho1", "Cafetera eléctrica", Categoria.HOGAR, 159.00,
            "Prepara hasta 12 tazas con filtro lavable.", "☕", 68, setOf("cocina")),
        Producto("ho2", "Lámpara LED de escritorio", Categoria.HOGAR, 79.00,
            "Tres tonos de luz y brazo flexible.", "💡", 56, setOf("escritorio", "decoracion")),
        Producto("ho3", "Sartén antiadherente", Categoria.HOGAR, 69.00,
            "28 cm, apta para todo tipo de cocinas.", "🍳", 52, setOf("cocina")),
        Producto("ho4", "Organizador multiuso", Categoria.HOGAR, 45.00,
            "Cajas apilables para ropa o herramientas.", "📦", 44, setOf("ordenar")),
        Producto("ho5", "Set de velas aromáticas", Categoria.HOGAR, 35.00,
            "Lavanda, vainilla y cítricos.", "🕯️", 47, setOf("relax", "decoracion")),
        Producto("ho6", "Hervidor eléctrico", Categoria.HOGAR, 99.00,
            "1.7 litros con apagado automático.", "🍵", 50, setOf("cocina")),
        Producto("ho7", "Licuadora de 1.5 litros", Categoria.HOGAR, 189.00,
            "Vaso de vidrio y 5 velocidades.", "🥤", 67, setOf("cocina")),
        Producto("ho8", "Juego de sábanas queen", Categoria.HOGAR, 129.00,
            "Algodón suave de 300 hilos.", "🛏️", 50, setOf("relax")),
        Producto("ho9", "Aspiradora robot", Categoria.HOGAR, 799.00,
            "Programable y con mapeo de la casa.", "🤖", 60, setOf("ordenar", "movil")),
        Producto("ho10", "Difusor de aromas", Categoria.HOGAR, 89.00,
            "Humidificador ultrasónico con luz suave.", "🌿", 48, setOf("relax", "decoracion")),
        Producto("ho11", "Set de cuchillos de cocina", Categoria.HOGAR, 119.00,
            "6 piezas de acero inoxidable con base.", "🔪", 52, setOf("cocina")),
        Producto("ho12", "Planta decorativa artificial", Categoria.HOGAR, 59.00,
            "Maceta incluida, no necesita cuidados.", "🌵", 45, setOf("decoracion", "relax"))
    )

    private val indice: Map<String, Producto> = productos.associateBy { it.id }

    fun porId(id: String?): Producto? = if (id == null) null else indice[id]

    /** Busca en nombre, descripción, categoría y etiquetas, sin importar mayúsculas ni tildes. */
    fun buscar(consulta: String, categoria: Categoria? = null): List<Producto> {
        val q = normalizar(consulta)
        return productos.filter { p ->
            (categoria == null || p.categoria == categoria) &&
                (q.isEmpty() ||
                    normalizar(p.nombre).contains(q) ||
                    normalizar(p.descripcion).contains(q) ||
                    normalizar(p.categoria.etiqueta).contains(q) ||
                    p.etiquetas.any { normalizar(it).contains(q) })
        }
    }

    /** El producto que mejor responde a una búsqueda: primero los que tienen el texto en el nombre y luego los más populares. */
    fun mejorCoincidencia(consulta: String): Producto? {
        val q = normalizar(consulta)
        if (q.isEmpty()) return null
        return buscar(consulta).sortedWith(
            compareByDescending<Producto> { normalizar(it.nombre).contains(q) }.thenByDescending { it.popularidad }
        ).firstOrNull()
    }

    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .trim()
}
