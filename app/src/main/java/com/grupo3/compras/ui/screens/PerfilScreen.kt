package com.grupo3.compras.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.data.Catalogo
import com.grupo3.compras.data.InteraccionEntity
import com.grupo3.compras.domain.Actividad
import com.grupo3.compras.domain.AnalisisCategoria
import com.grupo3.compras.domain.Contexto
import com.grupo3.compras.domain.Franja
import com.grupo3.compras.domain.TipoInteraccion
import com.grupo3.compras.ui.ComprasViewModel
import com.grupo3.compras.ui.components.CabeceraGradiente
import com.grupo3.compras.ui.components.ChipRazon
import com.grupo3.compras.ui.components.EstadoVacio
import com.grupo3.compras.ui.theme.color
import com.grupo3.compras.ui.tiempoRelativo
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PerfilScreen(
    vm: ComprasViewModel,
    irAParaTi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resultado by vm.resultado.collectAsStateWithLifecycle()
    val contexto by vm.contexto.collectAsStateWithLifecycle()
    val historial by vm.historial.collectAsStateWithLifecycle()
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()
    var verHistorial by rememberSaveable { mutableStateOf(false) }
    var confirmarBorrado by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "cabecera") {
            CabeceraGradiente {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) { Text("🛍️", fontSize = 28.sp) }
                    Column {
                        Text(
                            text = "Tu perfil de compras",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Todo se guarda solo en este teléfono.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
        item(key = "aprendizaje") {
            TarjetaAprendizaje(resultado.analisis, contexto, Modifier.padding(horizontal = 16.dp))
        }
        item(key = "privacidad") {
            TarjetaPrivacidad(
                interacciones = historial.size,
                favoritos = favoritos.size,
                onVerHistorial = { verHistorial = true },
                onBorrar = { confirmarBorrado = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item(key = "escenarios") {
            TarjetaEscenarios(
                onNuevo = { vm.borrarTodo(); irAParaTi() },
                onEjemplo = { vm.cargarHistorialEjemplo(); irAParaTi() },
                onCambio = { vm.cargarCambioDeGustos(); irAParaTi() },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    if (verHistorial) {
        HistorialSheet(filas = historial, onBorrar = vm::borrarInteraccion, onCerrar = { verHistorial = false })
    }
    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar todos tus datos?") },
            text = { Text("Se eliminarán tu historial, tus favoritos y lo que ya te mostramos. La app volverá a empezar de cero. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.borrarTodo()
                    confirmarBorrado = false
                }) { Text("Borrar todo", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SeccionCard(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun TarjetaAprendizaje(analisis: List<AnalisisCategoria>, contexto: Contexto, modifier: Modifier = Modifier) {
    val maxHistorico = analisis.maxOfOrNull { it.historico } ?: 0.0
    SeccionCard("🧠 Cómo aprende la app", modifier) {
        Text(
            text = "Contexto actual: ${contexto.franja.emoji} ${contexto.franja.etiqueta} · ${contexto.actividad.emoji} ${contexto.actividad.etiqueta}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                text = "P(Categoría | Contexto) ∝ P(Contexto | Categoría) × P(Categoría)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
        analisis.forEach { FilaCategoria(it, maxHistorico) }
        Text(
            text = "Pesos: búsqueda 1 · clic 2 · favorito 3. Cada 7 días un punto vale la mitad, así la app se adapta cuando cambian tus gustos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaCategoria(analisis: AnalisisCategoria, maxHistorico: Double) {
    val progreso = if (maxHistorico > 0) (analisis.historico / maxHistorico).toFloat() else 0f
    val animado by animateFloatAsState(targetValue = progreso, label = "barraCategoria")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${analisis.categoria.emoji} ${analisis.categoria.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format(Locale.US, "%.1f pts", analisis.historico),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { animado },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = analisis.categoria.color(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ChipRazon("Naive Bayes ${(analisis.naiveBayes * 100).roundToInt()} %")
            if (analisis.regla > 0) ChipRazon("Regla del grupo +", destacado = true)
        }
    }
}

@Composable
private fun TarjetaPrivacidad(
    interacciones: Int,
    favoritos: Int,
    onVerHistorial: () -> Unit,
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    SeccionCard("🔒 Privacidad y control", modifier) {
        FilaDato("📵", "Sin permiso de Internet", "La app no puede enviar tu historial a ningún servidor.")
        FilaDato("☁️", "Sin copia en la nube", "Las copias de seguridad automáticas están desactivadas.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Estadistica("$interacciones", "interacciones", Modifier.weight(1f))
            Estadistica("$favoritos", "favoritos", Modifier.weight(1f))
        }
        OutlinedButton(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
            Text("Ver mi historial")
        }
        OutlinedButton(
            onClick = onBorrar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Borrar todos mis datos")
        }
    }
}

@Composable
private fun FilaDato(emoji: String, titulo: String, detalle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(emoji, fontSize = 24.sp)
        Column {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Estadistica(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TarjetaEscenarios(
    onNuevo: () -> Unit,
    onEjemplo: () -> Unit,
    onCambio: () -> Unit,
    modifier: Modifier = Modifier
) {
    SeccionCard("🧪 Escenarios de prueba", modifier) {
        Text(
            text = "Cada botón reemplaza tu historial y te lleva a «Para ti» para ver la reacción de la app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Escenario("🆕", "Usuario nuevo", "Sin historial: decide solo por la hora y la popularidad.", onNuevo)
        Escenario("📚", "Historial de ejemplo", "Trabaja con tecnología y oficina; el fin de semana se entretiene.", onEjemplo)
        Escenario("🔄", "Cambio de gustos", "Antes deporte, ahora tecnología: lo reciente pesa más.", onCambio)
    }
}

@Composable
private fun Escenario(emoji: String, titulo: String, detalle: String, onClick: () -> Unit) {
    val forma = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 26.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorialSheet(
    filas: List<InteraccionEntity>,
    onBorrar: (Long) -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "Mi historial",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            text = "Esto es todo lo que la app sabe de ti. Puedes borrar cualquier fila.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        if (filas.isEmpty()) {
            EstadoVacio("🗒️", "Sin historial", "Cuando abras productos o busques, aparecerá aquí.")
        } else {
            LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                items(filas, key = { it.id }) { fila ->
                    val producto = Catalogo.porId(fila.productoId)
                    val tipo = TipoInteraccion.entries.firstOrNull { it.name == fila.interaccion }
                    val franja = Franja.entries.firstOrNull { it.name == fila.franja }
                    val actividad = Actividad.entries.firstOrNull { it.name == fila.actividad }
                    ListItem(
                        headlineContent = {
                            Text("${tipo?.etiqueta ?: fila.interaccion} · ${producto?.nombre ?: "“${fila.termino}”"}")
                        },
                        supportingContent = {
                            Text(
                                "${tiempoRelativo(fila.fecha)} · ${franja?.emoji ?: ""} ${franja?.etiqueta ?: fila.franja}" +
                                    " · ${actividad?.emoji ?: ""} ${actividad?.etiqueta ?: fila.actividad} · ${fila.peso} pt(s)"
                            )
                        },
                        leadingContent = { Text(producto?.emoji ?: "🔎", fontSize = 24.sp) },
                        trailingContent = {
                            IconButton(onClick = { onBorrar(fila.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Borrar esta fila")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
