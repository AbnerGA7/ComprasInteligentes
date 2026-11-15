# Compras Inteligentes (Grupo 3)

Recomendador on-device de productos. El historial (búsquedas, clics, favoritos) se guarda en Room
y el puntaje se calcula en el teléfono; no se envía nada a un servidor.

## Cómo generar el APK
1. Abrir la carpeta `ComprasInteligentes` en Android Studio (Ladybug o superior).
2. Build > Build App Bundle(s) / APK(s) > Build APK(s).
3. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Reglas del recomendador
- Pesos: búsqueda 1, clic 2, favorito 3.
- Horario laboral (lun-vie 9-17): bono x1.5 a productos de oficina.
- Fin de semana: bono x1.5 a productos de entretenimiento.
- Penalización x0.5 a los productos mostrados recientemente (evita repetir siempre lo mismo).
- Se muestran 3 recomendaciones.

Lógica pura en `domain/Recomendador.kt`.
