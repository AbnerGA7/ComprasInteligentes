package com.grupo3.compras.domain

import java.time.DayOfWeek
import java.time.LocalDateTime

enum class Categoria(val etiqueta: String, val emoji: String) {
    OFICINA("Oficina", "💼"),
    ENTRETENIMIENTO("Entretenimiento", "🎮"),
    TECNOLOGIA("Tecnología", "💻"),
    HOGAR("Hogar", "🏠"),
    DEPORTE("Deporte", "🏃")
}

/** Cada tipo de interacción tiene un peso: no todas las acciones expresan el mismo interés. */
enum class TipoInteraccion(val peso: Int, val etiqueta: String) {
    BUSQUEDA(1, "Búsqueda"),
    CLIC(2, "Clic"),
    FAVORITO(3, "Favorito")
}

enum class Actividad(val etiqueta: String, val emoji: String) {
    SENTADO("Sentado", "🪑"),
    CAMINANDO("Caminando", "🚶")
}

enum class Franja(val etiqueta: String, val emoji: String) {
    LABORAL("Horario laboral", "💼"),
    FUERA_DE_HORARIO("Fuera de horario", "🌙"),
    FIN_DE_SEMANA("Fin de semana", "🎉");

    companion object {
        fun de(fecha: LocalDateTime): Franja = when {
            fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY -> FIN_DE_SEMANA
            fecha.hour in 9..17 -> LABORAL
            else -> FUERA_DE_HORARIO
        }
    }
}

data class Contexto(val fechaHora: LocalDateTime, val actividad: Actividad) {
    val franja: Franja get() = Franja.de(fechaHora)
    val hora: Int get() = fechaHora.hour
}

data class Producto(
    val id: String,
    val nombre: String,
    val categoria: Categoria,
    val precio: Double,
    val descripcion: String,
    val emoji: String,
    val precioAnterior: Double? = null,
    val popularidad: Int = 50
) {
    val enOferta: Boolean get() = precioAnterior != null
    val descuento: Int get() = precioAnterior?.let { ((1 - precio / it) * 100).toInt() } ?: 0
}

/** Interacción ya convertida a dominio. [epochMillis] es el instante real; [franja] y [actividad] el contexto en que ocurrió. */
data class Interaccion(
    val productoId: String?,
    val categoria: Categoria,
    val tipo: TipoInteraccion,
    val epochMillis: Long,
    val actividad: Actividad,
    val franja: Franja,
    val peso: Int
)

data class Recomendacion(
    val producto: Producto,
    val puntaje: Double,
    val razones: List<String>,
    val descubrimiento: Boolean
)

data class AnalisisCategoria(
    val categoria: Categoria,
    val historico: Double,
    val naiveBayes: Double,
    val regla: Double
)

data class ResultadoMotor(
    val recomendaciones: List<Recomendacion>,
    val analisis: List<AnalisisCategoria>
)
