package com.grupo3.compras.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.domain.Franja
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val FormatoDia: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE HH:mm", Locale.forLanguageTag("es-PE"))

private fun explicacion(franja: Franja): String = when (franja) {
    Franja.LABORAL -> "Es horario laboral: te mostramos primero artículos de oficina."
    Franja.FIN_DE_SEMANA -> "Es fin de semana: te mostramos primero productos de entretenimiento."
    Franja.OTRO -> "Fuera de horario laboral: te recomendamos según lo que más te interesa en este momento."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecomendadosScreen(vm: ComprasViewModel, modifier: Modifier = Modifier) {
    val recomendaciones by vm.recomendaciones.collectAsStateWithLifecycle()
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()
    val franja by vm.franja.collectAsStateWithLifecycle()
    val ahora by vm.ahora.collectAsStateWithLifecycle()
    val modo by vm.modoHora.collectAsStateWithLifecycle()
    val gustos by vm.gustos.collectAsStateWithLifecycle()
    val relacionados by vm.relacionados.collectAsStateWithLifecycle()
    val probabilidades by vm.probabilidades.collectAsStateWithLifecycle()
    val contexto by vm.contexto.collectAsStateWithLifecycle()
    var confirmarBorrado by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Compras inteligentes", style = MaterialTheme.typography.headlineSmall)
        }

        // Contexto: día y hora
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val dia = ahora.format(FormatoDia).replaceFirstChar { it.uppercase() }
                    Text(
                        "$dia · ${franja.etiqueta}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        explicacion(franja),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Probar con otro momento",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModoHora.entries.forEach { opcion ->
                        FilterChip(
                            selected = modo == opcion,
                            onClick = { vm.elegirModoHora(opcion) },
                            label = { Text(opcion.etiqueta) }
                        )
                    }
                }
            }
        }

        // Las 3 recomendaciones
        item { TituloSeccion("Recomendados para ti", Modifier.padding(top = 6.dp)) }
        if (recomendaciones.isEmpty()) {
            item { EstadoVacio("No hay más productos", "Ya marcaste todo el catálogo como favorito.") }
        } else {
            items(recomendaciones, key = { it.producto.id }) { rec ->
                FilaProducto(
                    producto = rec.producto,
                    esFavorito = rec.producto.id in favoritos,
                    motivo = rec.motivo,
                    onClick = { vm.abrirProducto(rec.producto) },
                    onFavorito = { vm.alternarFavorito(rec.producto) }
                )
            }
        }
        item {
            OutlinedButton(onClick = vm::verOtras, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ver otras recomendaciones")
            }
        }

        // Relacionados con lo que buscó o guardó (cambian al instante, sin importar la hora)
        if (relacionados.isNotEmpty()) {
            item { TituloSeccion("Relacionados con lo que buscaste y guardaste", Modifier.padding(top = 10.dp)) }
            items(relacionados, key = { "rel-" + it.producto.id }) { rec ->
                FilaProducto(
                    producto = rec.producto,
                    esFavorito = rec.producto.id in favoritos,
                    motivo = rec.motivo,
                    onClick = { vm.abrirProducto(rec.producto) },
                    onFavorito = { vm.alternarFavorito(rec.producto) }
                )
            }
        }

        // Lo que aprendió
        item { TituloSeccion("Lo que he aprendido de ti", Modifier.padding(top = 10.dp)) }
        if (gustos.isEmpty()) {
            item {
                Text(
                    "Todavía no tengo datos. Busca productos, ábrelos o márcalos como favoritos y las recomendaciones se ajustan solas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item { Subtitulo("Tus puntos por categoría") }
            val maximo = gustos.first().second
            items(gustos, key = { "puntos-" + it.first.name }) { (categoria, puntos) ->
                BarraCategoria(
                    nombre = categoria.etiqueta,
                    valor = String.format(Locale.US, "%.1f pts", puntos),
                    progreso = (puntos / maximo).toFloat()
                )
            }
            item { Nota("Búsqueda = 1 punto, clic = 2, favorito = 3. Lo reciente pesa más.") }

            item {
                Subtitulo(
                    "Probabilidad ahora (Naive Bayes) · ${contexto.dia.etiqueta}, ${contexto.momento.etiqueta.lowercase()}"
                )
            }
            items(probabilidades, key = { "bayes-" + it.first.name }) { (categoria, probabilidad) ->
                BarraCategoria(
                    nombre = categoria.etiqueta,
                    valor = "${(probabilidad * 100).roundToInt()} %",
                    progreso = probabilidad.toFloat()
                )
            }
            item { Nota("Naive Bayes calcula qué categoría es más probable según el día y el momento, usando tu historial.") }
        }

        item {
            TextButton(onClick = { confirmarBorrado = true }) { Text("Borrar mis datos") }
        }
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Borrar mis datos") },
            text = { Text("Se eliminan tus búsquedas, clics y favoritos de este teléfono.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.borrarMisDatos()
                    confirmarBorrado = false
                }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Subtitulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun Nota(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun BarraCategoria(nombre: String, valor: String, progreso: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nombre, style = MaterialTheme.typography.bodyMedium)
            Text(
                valor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progreso.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
