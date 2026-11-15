package com.grupo3.compras.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.compras.ui.components.DetalleProductoSheet
import com.grupo3.compras.ui.screens.CatalogoScreen
import com.grupo3.compras.ui.screens.FavoritosScreen
import com.grupo3.compras.ui.screens.ParaTiScreen
import com.grupo3.compras.ui.screens.PerfilScreen
import com.grupo3.compras.ui.theme.ComprasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Las cabeceras son azul oscuro, así que los íconos de la barra de estado van siempre claros.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent { ComprasTheme { AppRoot() } }
    }
}

private enum class Pestana(val titulo: String, val icono: ImageVector) {
    PARA_TI("Para ti", Icons.Filled.Home),
    CATALOGO("Catálogo", Icons.Filled.Search),
    FAVORITOS("Favoritos", Icons.Filled.Favorite),
    PERFIL("Perfil", Icons.Filled.Person)
}

@Composable
private fun AppRoot(vm: ComprasViewModel = viewModel()) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.PARA_TI) }
    val abierto by vm.productoAbierto.collectAsStateWithLifecycle()
    val favoritos by vm.favoritos.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = pestana == destino,
                        onClick = { pestana = destino },
                        icon = {
                            if (destino == Pestana.FAVORITOS && favoritos.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${favoritos.size}") } }) {
                                    Icon(destino.icono, contentDescription = null)
                                }
                            } else {
                                Icon(destino.icono, contentDescription = null)
                            }
                        },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }
    ) { relleno ->
        Crossfade(
            targetState = pestana,
            modifier = Modifier.padding(relleno),
            label = "pestanas"
        ) { actual ->
            when (actual) {
                Pestana.PARA_TI -> ParaTiScreen(
                    vm = vm,
                    onAbrir = vm::abrirProducto,
                    irAPerfil = { pestana = Pestana.PERFIL }
                )

                Pestana.CATALOGO -> CatalogoScreen(vm = vm, onAbrir = vm::abrirProducto)

                Pestana.FAVORITOS -> FavoritosScreen(
                    vm = vm,
                    onAbrir = vm::abrirProducto,
                    irACatalogo = { pestana = Pestana.CATALOGO }
                )

                Pestana.PERFIL -> PerfilScreen(vm = vm, irAParaTi = { pestana = Pestana.PARA_TI })
            }
        }
    }

    abierto?.let { producto ->
        DetalleProductoSheet(
            producto = producto,
            esFavorito = producto.id in favoritos,
            onFavorito = { vm.alternarFavorito(producto) },
            onCerrar = vm::cerrarProducto
        )
    }
}
