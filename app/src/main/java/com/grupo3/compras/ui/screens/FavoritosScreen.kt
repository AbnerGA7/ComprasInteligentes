package com.grupo3.compras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.ui.ComprasViewModel
import com.grupo3.compras.ui.components.CabeceraGradiente
import com.grupo3.compras.ui.components.EstadoVacio
import com.grupo3.compras.ui.components.FilaProducto

@Composable
fun FavoritosScreen(
    vm: ComprasViewModel,
    onAbrir: (Producto) -> Unit,
    irACatalogo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val productos by vm.favoritosProductos.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGradiente {
            Text(
                text = "Favoritos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (productos.isEmpty()) "Aún no guardas nada." else "${productos.size} producto(s) guardados en tu teléfono.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        if (productos.isEmpty()) {
            EstadoVacio(
                emoji = "💙",
                titulo = "Tu lista de favoritos está vacía",
                detalle = "Toca el corazón en cualquier producto. Cada favorito le enseña más a la app (vale 3 puntos).",
                modifier = Modifier.padding(top = 24.dp),
                accion = { FilledTonalButton(onClick = irACatalogo) { Text("Explorar el catálogo") } }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    FilaProducto(
                        producto = producto,
                        esFavorito = true,
                        onAbrir = { onAbrir(producto) },
                        onFavorito = { vm.alternarFavorito(producto) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
