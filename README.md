# Compras Inteligentes (Grupo 3)

Recomendador **on-device** de productos para la Semana 11 (Laboratorio de Apps Autónomas y Edge AI).
Todo el historial vive en Room, dentro del teléfono: la app **no tiene permiso de Internet**.

## Descargar el APK
https://github.com/AbnerGA7/ComprasInteligentes/releases/download/apk-latest/app-debug.apk

## Lo que pide el grupo

| Requisito | Dónde se resuelve |
|---|---|
| Aprende de búsquedas, clics y favoritos | Cada acción se guarda en Room con su peso (búsqueda 1, clic 2, favorito 3). Las acciones recientes pesan más: cada 7 días un punto vale la mitad. `Recomendador.puntosPorCategoria` |
| En horario laboral recomienda artículos de oficina | Lunes a viernes de 9:00 a 17:59 se reservan 2 de los 3 lugares para Oficina. `Franja.de` y `Recomendador.recomendar` |
| En fin de semana recomienda entretenimiento | Sábado y domingo se reservan 2 de los 3 lugares para Entretenimiento |
| Evita recomendar siempre los mismos productos | Lo mostrado hace poco baja de posición (cada vez que se repite vale la mitad), máximo 2 por categoría, los favoritos no se vuelven a recomendar y el botón "Ver otras" rota las sugerencias |

## Flujo de la diapositiva 13
1. **Registrar**: `InteraccionEntity` con categoría, interacción, fecha, hora y peso.
2. **Calcular**: se acumulan los puntos por categoría y por producto y se ordenan.
3. **Contextualizar**: se ajusta según día y hora (laboral, fin de semana u otro).
4. **Mostrar**: lista con 3 recomendaciones y el motivo de cada una.

## Pantallas
- **Para ti**: contexto actual, las 3 recomendaciones, "Ver otras" y lo que la app ha aprendido. Los botones "Probar con otro momento" permiten simular horario laboral o fin de semana sin esperar al día.
- **Productos**: buscador y filtro por categoría (24 productos de ejemplo).
- **Favoritos**: lista de productos guardados.

## Pruebas
`app/src/test/.../RecomendadorTest.kt` tiene 18 pruebas del motor (contexto, pesos, decaimiento, favoritos, variedad, rotación y catálogo).
GitHub Actions las corre en cada push antes de armar el APK.

## Compilar
Abrir la carpeta en Android Studio y ejecutar Build > Build APK(s), o dejar que GitHub Actions lo compile en cada push.
