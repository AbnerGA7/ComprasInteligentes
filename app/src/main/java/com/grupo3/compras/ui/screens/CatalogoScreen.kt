package com.grupo3.compras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.data.Catalogo
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.ui.ComprasViewModel
import com.grupo3.compras.ui.components.CabeceraGradiente
import com.grupo3.compras.ui.components.EstadoVacio
import com.grupo3.compras.ui.components.TarjetaProducto
import kotlinx.coroutines.delay

@Composable
fun CatalogoScreen(
    vm: ComprasViewModel,
    onAbrir: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()
    var consulta by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf<Categoria?>(null) }
    val resultados = remember(consulta, categoria) { Catalogo.buscar(consulta, categoria) }
    val foco: FocusManager = LocalFocusManager.current

    // Aprender de lo que se busca: se registra cuando el usuario deja de escribir.
    LaunchedEffect(consulta) {
        if (consulta.trim().length >= 3) {
            delay(1_200)
            vm.registrarBusqueda(consulta)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        CabeceraGradiente {
            Text(
                text = "Catálogo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Lo que buscas también enseña a la app qué te interesa.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            TextField(
                value = consulta,
                onValueChange = { consulta = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Busca productos, ej. teclado") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (consulta.isNotEmpty()) {
                    {
                        IconButton(onClick = { consulta = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                        }
                    }
                } else null,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    vm.registrarBusqueda(consulta)
                    foco.clearFocus()
                })
            )
        }

        val opciones: List<Categoria?> = listOf(null) + Categoria.entries
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(opciones) { opcion ->
                FilterChip(
                    selected = categoria == opcion,
                    onClick = { categoria = opcion },
                    label = { Text(if (opcion == null) "Todos" else "${opcion.emoji} ${opcion.etiqueta}") }
                )
            }
        }

        if (resultados.isEmpty()) {
            EstadoVacio(
                emoji = "🔎",
                titulo = "No encontramos productos",
                detalle = "Prueba con otra palabra o cambia la categoría."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultados, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        esFavorito = producto.id in favoritos,
                        onAbrir = { onAbrir(producto) },
                        onFavorito = { vm.alternarFavorito(producto) }
                    )
                }
            }
        }
    }
}
