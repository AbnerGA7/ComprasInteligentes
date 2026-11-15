package com.grupo3.compras.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.domain.Actividad
import com.grupo3.compras.domain.Contexto
import com.grupo3.compras.domain.Franja
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.ui.ComprasViewModel
import com.grupo3.compras.ui.ModoActividad
import com.grupo3.compras.ui.ModoHora
import com.grupo3.compras.ui.components.CabeceraGradiente
import com.grupo3.compras.ui.components.EstadoVacio
import com.grupo3.compras.ui.components.TarjetaCarrusel
import com.grupo3.compras.ui.components.TarjetaRecomendacion
import com.grupo3.compras.ui.components.TituloSeccion
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ParaTiScreen(
    vm: ComprasViewModel,
    onAbrir: (Producto) -> Unit,
    irAPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resultado by vm.resultado.collectAsStateWithLifecycle()
    val contexto by vm.contexto.collectAsStateWithLifecycle()
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()
    val modoHora by vm.modoHora.collectAsStateWithLifecycle()
    val modoActividad by vm.modoActividad.collectAsStateWithLifecycle()

    val recomendaciones = resultado.recomendaciones
    val ids = recomendaciones.map { it.producto.id }
    LaunchedEffect(ids) { vm.registrarLote(ids) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "cabecera") { CabeceraParaTi(contexto) }
        item(key = "contexto") {
            PanelContexto(
                modoHora = modoHora,
                modoActividad = modoActividad,
                acelerometroDisponible = vm.acelerometroDisponible,
                onHora = vm::elegirModoHora,
                onActividad = vm::elegirModoActividad,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item(key = "titulo") {
            TituloSeccion(
                texto = "Recomendado para ti",
                modifier = Modifier.padding(horizontal = 16.dp),
                accion = {
                    TextButton(onClick = vm::mostrarOtras) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ver otras")
                    }
                }
            )
        }
        when {
            recomendaciones.isEmpty() -> item(key = "vacio") {
                EstadoVacio(
                    emoji = "🛍️",
                    titulo = "Preparando tus recomendaciones",
                    detalle = "Si ya guardaste todo lo relevante como favorito, explora el catálogo para descubrir más."
                )
            }

            contexto.actividad == Actividad.CAMINANDO -> item(key = "carrusel") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🚶 Formato rápido: desliza para ver tus 3 sugerencias",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recomendaciones, key = { it.producto.id }) { r ->
                            TarjetaCarrusel(
                                recomendacion = r,
                                esFavorito = r.producto.id in favoritos,
                                onAbrir = { onAbrir(r.producto) },
                                onFavorito = { vm.alternarFavorito(r.producto) }
                            )
                        }
                    }
                }
            }

            else -> items(recomendaciones, key = { it.producto.id }) { r ->
                TarjetaRecomendacion(
                    recomendacion = r,
                    esFavorito = r.producto.id in favoritos,
                    onAbrir = { onAbrir(r.producto) },
                    onFavorito = { vm.alternarFavorito(r.producto) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem()
                )
            }
        }
        item(key = "explicacion") {
            ExplicacionCard(contexto, irAPerfil, Modifier.padding(horizontal = 16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CabeceraParaTi(contexto: Contexto) {
    val saludo = when (contexto.hora) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val fecha = remember(contexto.fechaHora) {
        contexto.fechaHora.format(DateTimeFormatter.ofPattern("EEE HH:mm", Locale("es", "PE")))
    }
    CabeceraGradiente {
        Text(
            text = "$saludo 👋",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
        Text(
            text = "Compras inteligentes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Aprendo de tus búsquedas, clics y favoritos para sugerirte lo que necesitas justo ahora.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PildoraBlanca("${contexto.franja.emoji} ${contexto.franja.etiqueta}")
            PildoraBlanca("${contexto.actividad.emoji} ${contexto.actividad.etiqueta}")
            PildoraBlanca("🕒 $fecha")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Se procesa en tu teléfono · sin Internet",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PildoraBlanca(texto: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PanelContexto(
    modoHora: ModoHora,
    modoActividad: ModoActividad,
    acelerometroDisponible: Boolean,
    onHora: (ModoHora) -> Unit,
    onActividad: (ModoActividad) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by rememberSaveable { mutableStateOf(true) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contexto de la demo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cambia la hora o la actividad y mira cómo se adapta la recomendación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (expandido) "Ocultar" else "Mostrar",
                    modifier = Modifier.rotate(if (expandido) 180f else 0f)
                )
            }
            if (expandido) {
                Text("Hora del día", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModoHora.entries.forEach { modo ->
                        val seleccionado = modoHora == modo
                        FilterChip(
                            selected = seleccionado,
                            onClick = { onHora(modo) },
                            label = { Text(modo.etiqueta) },
                            leadingIcon = if (seleccionado) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                    }
                }
                Text("Actividad", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModoActividad.entries
                        .filter { it != ModoActividad.AUTO || acelerometroDisponible }
                        .forEach { modo ->
                            val seleccionado = modoActividad == modo
                            FilterChip(
                                selected = seleccionado,
                                onClick = { onActividad(modo) },
                                label = { Text(modo.etiqueta) },
                                leadingIcon = if (seleccionado) {
                                    { Icon(Icons.Filled.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                                } else null
                            )
                        }
                }
                if (modoActividad == ModoActividad.AUTO) {
                    Text(
                        text = "Auto usa el acelerómetro: camina con el teléfono en la mano o el bolsillo para probarlo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplicacionCard(contexto: Contexto, irAPerfil: () -> Unit, modifier: Modifier = Modifier) {
    val regla = when (contexto.franja) {
        Franja.LABORAL -> "En horario laboral priorizo artículos de oficina."
        Franja.FIN_DE_SEMANA -> "En fin de semana priorizo productos de entretenimiento."
        Franja.FUERA_DE_HORARIO -> "Fuera de horario priorizo entretenimiento y hogar."
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "¿Cómo decidí esto?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val estilo = MaterialTheme.typography.bodySmall
            val color = MaterialTheme.colorScheme.onPrimaryContainer
            Text("• $regla", style = estilo, color = color)
            Text("• Aprendo de tus búsquedas (1 pt), clics (2 pts) y favoritos (3 pts). Con los días pesan menos.", style = estilo, color = color)
            Text("• No repito siempre lo mismo: lo que ya te mostré baja de posición.", style = estilo, color = color)
            Text("• Si caminas, cambio a un carrusel rápido con ofertas.", style = estilo, color = color)
            TextButton(onClick = irAPerfil) { Text("Ver cómo aprende →") }
        }
    }
}
