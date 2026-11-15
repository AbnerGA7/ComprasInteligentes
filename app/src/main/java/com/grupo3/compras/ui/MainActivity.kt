package com.grupo3.compras.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.compras.domain.Recomendacion
import com.grupo3.compras.domain.TipoInteraccion

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PantallaCompras() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCompras(vm: ComprasViewModel = viewModel()) {
    val recomendaciones by vm.recomendaciones.collectAsState()
    val productos by vm.productos.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Compras inteligentes") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Recomendado para ti", style = MaterialTheme.typography.titleLarge)
            recomendaciones.forEach { TarjetaRecomendacion(it) }

            HorizontalDivider()
            Text("Tu interacción de hoy", style = MaterialTheme.typography.titleMedium)
            productos.forEach { p ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(p.nombre, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.registrar(p.id, TipoInteraccion.BUSQUEDA) }) { Text("Buscar") }
                    TextButton(onClick = { vm.registrar(p.id, TipoInteraccion.CLIC) }) { Text("Clic") }
                    TextButton(onClick = { vm.registrar(p.id, TipoInteraccion.FAVORITO) }) { Text("★") }
                }
            }
        }
    }
}

@Composable
fun TarjetaRecomendacion(r: Recomendacion) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(r.producto.nombre, style = MaterialTheme.typography.titleMedium)
            Text(r.justificacion, style = MaterialTheme.typography.bodySmall)
        }
    }
}
