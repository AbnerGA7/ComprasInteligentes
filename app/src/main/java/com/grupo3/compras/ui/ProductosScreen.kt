package com.grupo3.compras.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo3.compras.data.Catalogo
import com.grupo3.compras.domain.Categoria
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(vm: ComprasViewModel, modifier: Modifier = Modifier) {
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()
    var consulta by rememberSaveable { mutableStateOf("") }
    var categoriaElegida by rememberSaveable { mutableStateOf<String?>(null) }
    val foco = LocalFocusManager.current

    val categoria = categoriaElegida?.let { Categoria.valueOf(it) }
    val resultados = Catalogo.buscar(consulta, categoria)

    // Cuando el usuario deja de escribir, la búsqueda se guarda para aprender de ella.
    LaunchedEffect(consulta) {
        delay(1_000)
        vm.registrarBusqueda(consulta)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Productos", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = consulta,
                onValueChange = { consulta = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar productos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { consulta = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    vm.registrarBusqueda(consulta)
                    foco.clearFocus()
                })
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoria == null,
                    onClick = { categoriaElegida = null },
                    label = { Text("Todos") }
                )
                Categoria.entries.forEach { c ->
                    FilterChip(
                        selected = categoria == c,
                        onClick = { categoriaElegida = if (categoria == c) null else c.name },
                        label = { Text(c.etiqueta) }
                    )
                }
            }
        }

        if (resultados.isEmpty()) {
            EstadoVacio("Sin resultados", "No encontramos productos para «${consulta.trim()}».")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(resultados, key = { it.id }) { producto ->
                    FilaProducto(
                        producto = producto,
                        esFavorito = producto.id in favoritos,
                        onClick = { vm.abrirProducto(producto) },
                        onFavorito = { vm.alternarFavorito(producto) }
                    )
                }
            }
        }
    }
}
