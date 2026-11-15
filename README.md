# Compras Inteligentes (Grupo 3)

Recomendador **on-device** de productos para la Semana 11 (Laboratorio de Apps Autónomas y Edge AI).
Todo el historial vive en Room, dentro del teléfono: la app **no tiene permiso de Internet**.

## Descargar el APK
https://github.com/AbnerGA7/ComprasInteligentes/releases/download/apk-latest/app-debug.apk

## Lo que pide el grupo

| Requisito | Dónde se resuelve |
|---|---|
| Aprende de búsquedas, clics y favoritos | Cada acción se guarda en Room con su peso (búsqueda 1, clic 2, favorito 3), la hora y el día de la semana. Las acciones recientes pesan más: cada 7 días un punto vale la mitad. `Recomendador.puntosPorCategoria` |
| En horario laboral recomienda artículos de oficina | Lunes a viernes de 9:00 a 17:59 se reservan 2 de los 3 lugares para Oficina. `Franja.de` y `Recomendador.recomendar` |
| En fin de semana recomienda entretenimiento | Sábado y domingo se reservan 2 de los 3 lugares para Entretenimiento |
| Evita recomendar siempre los mismos productos | Lo mostrado hace poco baja de posición (cada vez que se repite vale la mitad), máximo 2 por categoría, los favoritos no se vuelven a recomendar y el botón "Ver otras" rota las sugerencias |

## Flujo de la diapositiva 13
1. **Registrar**: `InteraccionEntity` con categoría, interacción, fecha, hora y peso.
2. **Calcular**: se acumulan los puntos por categoría y por producto y se ordenan.
3. **Contextualizar**: se ajusta según día y hora (laboral, fin de semana u otro) y Naive Bayes estima qué categoría es más probable en ese momento.
4. **Mostrar**: lista con 3 recomendaciones y el motivo de cada una.

## Naive Bayes
`NaiveBayes.kt` estima `P(Categoría | Contexto) ∝ P(Categoría) × P(Día | Categoría) × P(Momento | Categoría)`.
El contexto es el tipo de día (entre semana o fin de semana) y el momento (mañana, tarde o noche).
En lugar de contar cada acción como 1, cuenta su peso (búsqueda 1, clic 2, favorito 3, con olvido de 7 días) y usa suavizado de Laplace para evitar probabilidades de 0.

Puntaje de cada producto = 35 % lo que te interesó ese producto + 25 % parecido a lo que ya buscaste o guardaste + 25 % probabilidad Naive Bayes de su categoría + 15 % popularidad.
Las reglas del grupo (horario laboral → oficina, fin de semana → entretenimiento) siguen reservando 2 de los 3 lugares.

## Parecidos: buscar o guardar se nota al instante
Cada producto tiene etiquetas (por ejemplo `computo`, `escritorio`, `cocina`, `musica`) que lo relacionan con otros, incluso de otras categorías.
Al buscar, abrir o guardar un producto, suben los que comparten etiquetas con él (similitud de Jaccard, ponderada por los puntos de la acción).
Una búsqueda se guarda sobre el producto que mejor coincide con el texto (el que lo tiene en el nombre y es más popular).
La sección **Relacionados** de "Para ti" muestra productos nuevos parecidos a lo que hiciste, uno por cada cosa que tocaste, sin importar la hora.

## Pantallas
- **Para ti**: contexto actual, las 3 recomendaciones, "Ver otras", productos relacionados con lo que buscaste y guardaste, y lo que la app ha aprendido (puntos por categoría y probabilidad de Naive Bayes). Los botones "Probar con otro momento" permiten simular horario laboral o fin de semana sin esperar al día.
- **Productos**: buscador y filtro por categoría (48 productos de ejemplo, 12 por categoría).
- **Favoritos**: lista de productos guardados.

## Pruebas
`app/src/test/.../RecomendadorTest.kt` tiene 35 pruebas del motor (contexto, pesos, decaimiento, Naive Bayes, parecidos, favoritos, variedad, rotación y catálogo).
GitHub Actions las corre en cada push antes de armar el APK.

## Compilar
Abrir la carpeta en Android Studio y ejecutar Build > Build APK(s), o dejar que GitHub Actions lo compile en cada push.
