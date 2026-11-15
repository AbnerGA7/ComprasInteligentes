package com.grupo3.compras.ui

import java.util.Locale

fun Double.soles(): String = "S/ " + String.format(Locale.US, "%,.2f", this)

fun tiempoRelativo(millis: Long, ahora: Long = System.currentTimeMillis()): String {
    val minutos = ((ahora - millis) / 60_000L).coerceAtLeast(0L)
    return when {
        minutos < 1 -> "ahora"
        minutos < 60 -> "hace $minutos min"
        minutos < 60 * 24 -> "hace ${minutos / 60} h"
        else -> "hace ${minutos / (60 * 24)} d"
    }
}
