package com.grupo3.compras.domain

import java.time.DayOfWeek
import java.time.LocalDateTime

enum class Categoria(val etiqueta: String) {
    OFICINA("Oficina"),
    ENTRETENIMIENTO("Entretenimiento"),
    TECNOLOGIA("Tecnología"),
    HOGAR("Hogar")
}

/** No todas las acciones expresan el mismo interés, por eso cada una tiene un peso. */
enum class TipoInteraccion(val peso: Int, val etiqueta: String) {
    BUSQUEDA(1, "Búsqueda"),
    CLIC(2, "Clic"),
    FAVORITO(3, "Favorito")
}

enum class Franja(val etiqueta: String) {
    LABORAL("Horario laboral"),
    FIN_DE_SEMANA("Fin de semana"),
    OTRO("Fuera de horario");

    companion object {
        /** Horario laboral: lunes a viernes de 9:00 a 17:59. */
        fun de(fecha: LocalDateTime): Franja = when {
            fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY -> FIN_DE_SEMANA
            fecha.hour in 9..17 -> LABORAL
            else -> OTRO
        }
    }
}

data class Producto(
    val id: String,
    val nombre: String,
    val categoria: Categoria,
    val precio: Double,
    val descripcion: String,
    val emoji: String,
    val popularidad: Int = 50
)

data class Interaccion(
    val productoId: String?,
    val categoria: Categoria,
    val tipo: TipoInteraccion,
    val epochMillis: Long
)

data class Recomendacion(
    val producto: Producto,
    val motivo: String
)
