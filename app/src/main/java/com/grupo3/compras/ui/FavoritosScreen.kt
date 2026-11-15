package com.grupo3.compras.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FavoritosScreen(vm: ComprasViewModel, modifier: Modifier = Modifier) {
    val productos by vm.favoritosProductos.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Favoritos", style = MaterialTheme.typography.headlineSmall) }
        if (productos.isEmpty()) {
            item {
                EstadoVacio(
                    "Aún no tienes favoritos",
                    "Toca el corazón en cualquier producto para guardarlo. Los favoritos pesan más al recomendar."
                )
            }
        } else {
            items(productos, key = { it.id }) { producto ->
                FilaProducto(
                    producto = producto,
                    esFavorito = true,
                    onClick = { vm.abrirProducto(producto) },
                    onFavorito = { vm.alternarFavorito(producto) }
                )
            }
        }
    }
}
