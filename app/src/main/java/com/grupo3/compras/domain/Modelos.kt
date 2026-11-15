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

enum class TipoDia(val etiqueta: String) {
    ENTRE_SEMANA("Entre semana"),
    FIN_DE_SEMANA("Fin de semana")
}

enum class Momento(val etiqueta: String) {
    MANANA("Mañana"),
    TARDE("Tarde"),
    NOCHE("Noche")
}

/** Lo que mira Naive Bayes: tipo de día y momento del día (dos datos que trata como independientes). */
data class Contexto(val dia: TipoDia, val momento: Momento) {
    companion object {
        fun de(fecha: LocalDateTime): Contexto = de(fecha.dayOfWeek, fecha.hour)

        /** Mañana 5:00–11:59, tarde 12:00–17:59, noche 18:00–4:59. */
        fun de(dia: DayOfWeek, hora: Int): Contexto = Contexto(
            if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) TipoDia.FIN_DE_SEMANA else TipoDia.ENTRE_SEMANA,
            when (hora) {
                in 5..11 -> Momento.MANANA
                in 12..17 -> Momento.TARDE
                else -> Momento.NOCHE
            }
        )
    }
}

data class Producto(
    val id: String,
    val nombre: String,
    val categoria: Categoria,
    val precio: Double,
    val descripcion: String,
    val emoji: String,
    val popularidad: Int = 50,
    /** Palabras que relacionan productos entre sí (por ejemplo "computo" o "cocina"), aunque sean de otra categoría. */
    val etiquetas: Set<String> = emptySet()
)

data class Interaccion(
    val productoId: String?,
    val categoria: Categoria,
    val tipo: TipoInteraccion,
    val epochMillis: Long,
    val contexto: Contexto
)

data class Recomendacion(
    val producto: Producto,
    val motivo: String
)
