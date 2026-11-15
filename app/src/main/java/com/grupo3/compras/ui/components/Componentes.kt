package com.grupo3.compras.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.domain.Recomendacion
import com.grupo3.compras.ui.soles
import com.grupo3.compras.ui.theme.GradienteCabecera
import com.grupo3.compras.ui.theme.color
import java.util.Locale

// ------------------------------------------------------------------ bloques básicos

@Composable
fun CabeceraGradiente(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(GradienteCabecera)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = contenido
    )
}

@Composable
fun EmojiTile(emoji: String, color: Color, tamano: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano * 0.28f))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.32f), color.copy(alpha = 0.10f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (tamano.value * 0.5f).sp)
    }
}

@Composable
fun EmojiBanner(emoji: String, color: Color, alto: Dp, tamanoEmoji: TextUnit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.10f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = tamanoEmoji)
    }
}

@Composable
fun PrecioTexto(producto: Producto, grande: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = producto.precio.soles(),
            style = if (grande) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        producto.precioAnterior?.let {
            Text(
                text = it.soles(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

@Composable
fun ChipRazon(texto: String, destacado: Boolean = false) {
    val fondo = if (destacado) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contenido = if (destacado) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(shape = CircleShape, color = fondo) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = contenido,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ChipCategoria(categoria: Categoria) {
    Surface(shape = CircleShape, color = categoria.color().copy(alpha = 0.16f)) {
        Text(
            text = "${categoria.emoji} ${categoria.etiqueta}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = categoria.color(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun BotonFavorito(esFavorito: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val escala by animateFloatAsState(
        targetValue = if (esFavorito) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaFavorito"
    )
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
            tint = if (esFavorito) Color(0xFFE5484D) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(escala)
        )
    }
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier, accion: (@Composable () -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        accion?.invoke()
    }
}

@Composable
fun EstadoVacio(
    emoji: String,
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = emoji, fontSize = 56.sp)
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        accion?.invoke()
    }
}

// ------------------------------------------------------------------ tarjetas de producto

@Composable
private fun TarjetaClicable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = MaterialTheme.shapes.large
    Card(
        modifier = modifier
            .clip(forma)
            .clickable(onClick = onClick),
        shape = forma,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = contenido
    )
}

/** Formato "lista": se usa cuando el usuario está sentado y puede leer con calma. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaRecomendacion(
    recomendacion: Recomendacion,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = recomendacion.producto
    TarjetaClicable(onClick = onAbrir, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EmojiTile(p.emoji, p.categoria.color(), 76.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = p.categoria.etiqueta.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = p.categoria.color(),
                        modifier = Modifier.weight(1f)
                    )
                    BotonFavorito(esFavorito, onFavorito, Modifier.size(32.dp))
                }
                Text(
                    text = p.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = p.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PrecioTexto(p)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recomendacion.razones.forEach { ChipRazon(it, destacado = it.startsWith("✨") || it.startsWith("🚶")) }
                }
                Text(
                    text = "Puntaje " + String.format(Locale.US, "%.2f", recomendacion.puntaje),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/** Formato "carrusel": compacto, para cuando el usuario camina y solo mira de reojo. */
@Composable
fun TarjetaCarrusel(
    recomendacion: Recomendacion,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = recomendacion.producto
    TarjetaClicable(onClick = onAbrir, modifier = modifier.width(272.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmojiTile(p.emoji, p.categoria.color(), 60.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = p.categoria.etiqueta.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = p.categoria.color()
                    )
                    Text(
                        text = p.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BotonFavorito(esFavorito, onFavorito, Modifier.size(36.dp))
            }
            PrecioTexto(p, grande = true)
            ChipRazon(recomendacion.razones.first(), destacado = recomendacion.razones.first().startsWith("🚶"))
            FilledTonalButton(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) {
                Text("Ver producto")
            }
        }
    }
}

/** Fila simple, usada en Favoritos. */
@Composable
fun FilaProducto(
    producto: Producto,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaClicable(onClick = onAbrir, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiTile(producto.emoji, producto.categoria.color(), 56.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${producto.categoria.emoji} ${producto.categoria.etiqueta}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrecioTexto(producto)
            }
            BotonFavorito(esFavorito, onFavorito)
        }
    }
}

/** Tarjeta vertical para la cuadrícula del catálogo. */
@Composable
fun TarjetaProducto(
    producto: Producto,
    esFavorito: Boolean,
    onAbrir: () -> Unit,
    onFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaClicable(onClick = onAbrir, modifier = modifier.fillMaxWidth()) {
        Box {
            EmojiBanner(producto.emoji, producto.categoria.color(), alto = 104.dp, tamanoEmoji = 54.sp)
            BotonFavorito(
                esFavorito = esFavorito,
                onClick = onFavorito,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            if (producto.enOferta) {
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "−${producto.descuento} %",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = producto.categoria.etiqueta.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = producto.categoria.color()
            )
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            PrecioTexto(producto)
        }
    }
}

// ------------------------------------------------------------------ detalle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoSheet(
    producto: Producto,
    esFavorito: Boolean,
    onFavorito: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiBanner(
                emoji = producto.emoji,
                color = producto.categoria.color(),
                alto = 170.dp,
                tamanoEmoji = 96.sp,
                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ChipCategoria(producto.categoria)
                if (producto.enOferta) ChipRazon("Oferta −${producto.descuento} %", destacado = true)
            }
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            PrecioTexto(producto, grande = true)
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onFavorito,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = if (esFavorito) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (esFavorito) "En tus favoritos" else "Agregar a favoritos")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tus clics y favoritos se guardan solo en este teléfono.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
