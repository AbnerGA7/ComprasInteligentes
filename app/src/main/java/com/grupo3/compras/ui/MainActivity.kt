package com.grupo3.compras.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // El tema es claro, así que los íconos de las barras del sistema van oscuros.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, 0x40000000)
        )
        setContent {
            ComprasTheme { Aplicacion() }
        }
    }
}

@Composable
private fun Aplicacion(vm: ComprasViewModel = viewModel()) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val abierto by vm.productoAbierto.collectAsStateWithLifecycle()
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Para ti") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Productos") }
                )
                NavigationBarItem(
                    selected = pestana == 2,
                    onClick = { pestana = 2 },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text("Favoritos") }
                )
            }
        }
    ) { relleno ->
        val modificador = Modifier.padding(relleno)
        when (pestana) {
            0 -> RecomendadosScreen(vm, modificador)
            1 -> ProductosScreen(vm, modificador)
            else -> FavoritosScreen(vm, modificador)
        }
    }

    abierto?.let { producto ->
        DetalleProducto(
            producto = producto,
            esFavorito = producto.id in favoritos,
            onFavorito = { vm.alternarFavorito(producto) },
            onCerrar = vm::cerrarProducto
        )
    }
}
