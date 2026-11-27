# Estrategia de entrega y rollback

## Comparación de estrategias

| Estrategia | Cómo funciona | Ventajas | Inconvenientes para BiblioTech |
|---|---|---|---|
| **Rolling** | Reemplaza las instancias una a una por la versión nueva | Sin servidor extra | Necesita varias instancias; con una sola no hay nada que ir reemplazando |
| **Blue-green** | Levanta la versión nueva al lado de la actual y cambia el tráfico cuando está sana | Sin caída; volver atrás es inmediato | Requiere duplicar recursos mientras dura el cambio |
| **Canary** | Envía un porcentaje pequeño del tráfico a la versión nueva y lo va aumentando | Limita el daño de un error | Exige repartir el tráfico y medir métricas; excesivo para un sistema pequeño |

## Estrategia elegida: blue-green

BiblioTech es un sistema pequeño, con una instancia de cada servicio y pocos usuarios simultáneos. Blue-green encaja porque:

- **No hay caída.** El hosting arranca la versión nueva, espera a que responda la comprobación de salud (`/api/libros`) y solo entonces le pasa el tráfico y detiene la anterior. Si la nueva no arranca, la anterior sigue atendiendo.
- **No necesita infraestructura de canary** (reparto de tráfico y métricas) que no tendría quién la use.
- **Rolling no aporta** con una sola instancia por servicio.

## Cómo se garantiza que se pueda volver atrás

1. Cada versión se publica con una **etiqueta inmutable** (`1.0.0`, `1.1.0`...) además de `latest`. La versión anterior siempre sigue disponible en el registro.
2. El despliegue ordena a la plataforma usar **la etiqueta exacta** de la versión, no `latest`, por lo que siempre se sabe qué está en producción.
3. Los cambios en la base de datos deben ser **compatibles hacia atrás** (agregar columnas o tablas, no borrarlas ni renombrarlas en la misma versión). Así la versión anterior sigue funcionando con el esquema nuevo.

## Procedimiento de rollback

1. En GitHub: **Actions → BiblioTech CD → Run workflow**.
2. En *Versión ya publicada que se quiere desplegar* escribir la versión estable anterior, por ejemplo `1.0.0`, y ejecutar.
3. El workflow **no construye nada**: solo pide al hosting desplegar las imágenes `1.0.0` del backend y del frontend y comprueba por HTTPS que `/api/libros` responde 200.
4. Si el hosting lo permite, también se puede usar su panel (*Manual Deploy* → imagen anterior).

**Tiempo estimado de recuperación:** el que tarda el hosting en arrancar la imagen anterior, normalmente pocos minutos.

**Límite:** el rollback restituye el código, no los datos. Por eso los cambios de esquema se hacen siempre compatibles hacia atrás.
