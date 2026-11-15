# Compras Inteligentes (Grupo 3)

Recomendador **on-device** de productos para la Semana 11 (Laboratorio de Apps Autónomas y Edge AI).
La app aprende de búsquedas, clics y favoritos, y adapta lo que muestra según la hora y la actividad.
Todo el historial vive en Room, dentro del teléfono: la app **no tiene permiso de Internet**.

## Descargar el APK
https://github.com/AbnerGA7/ComprasInteligentes/releases/download/apk-latest/app-debug.apk

## Cómo funciona (diapositiva 13)
1. **Registrar**: entidad Room con categoría, interacción, fecha, actividad, hora, franja y peso.
2. **Calcular**: puntaje por categoría (búsqueda 1, clic 2, favorito 3) con decaimiento: cada 7 días un punto vale la mitad.
3. **Contextualizar**: Naive Bayes `P(Categoría | Contexto) ∝ P(Contexto | Categoría) × P(Categoría)` más las reglas del grupo:
   horario laboral → oficina, fin de semana → entretenimiento.
4. **Mostrar**: 3 recomendaciones. Sentado → lista; caminando → carrusel rápido con ofertas.

Evita repetir siempre lo mismo: lo mostrado recientemente baja de posición, el tercer lugar prueba una categoría nueva
y "Ver otras" rota las sugerencias.

## Privacidad
Sin Internet, sin copia de seguridad en la nube, y desde Perfil se puede ver, borrar fila por fila o eliminar todo el historial.

## Escenarios de prueba (diapositiva 15)
Perfil → Escenarios: usuario nuevo, historial de ejemplo y cambio de gustos. En "Para ti" se puede simular la hora
(laboral, fin de semana, noche) y la actividad (sentado, caminando o automática con el acelerómetro).

## Compilar
Abrir la carpeta en Android Studio y ejecutar Build > Build APK(s), o dejar que GitHub Actions lo compile en cada push
(también corre las pruebas unitarias del motor).
