# Pruebas de rendimiento con JMeter

El plan `bibliotech-rendimiento.jmx` mide el tiempo de respuesta y el porcentaje de error de BiblioTech con usuarios concurrentes.

## Qué hace cada usuario virtual

1. **Preparación (una sola vez, antes de la carga):** registra un libro con 1 000 000 de ejemplares, para que ningún préstamo se rechace por falta de ejemplares.
2. **Al entrar:** el usuario virtual registra su propio estudiante (`POST /api/estudiantes`).
3. **En cada iteración:**
   - `GET /api/libros`
   - `POST /api/prestamos` (registra un préstamo)
   - `PUT /api/prestamos/{id}/devolucion` (lo devuelve, para que el estudiante no llegue al máximo de 3 préstamos activos)

Todas las peticiones verifican el código de respuesta esperado (200 o 201); un código distinto cuenta como error.

## Parámetros

| Parámetro | Por defecto | Qué controla |
|---|---|---|
| `usuarios` | 10 | Usuarios concurrentes |
| `iteraciones` | 10 | Repeticiones del flujo por usuario |
| `rampa` | 10 | Segundos en los que se incorporan todos los usuarios |
| `protocol` | `http` | `http` o `https` |
| `host` | `localhost` | Servidor a probar |
| `port` | `8080` | Puerto (`443` para HTTPS) |

## Ejecución

Con el sistema levantado (`docker compose up -d --build`):

```
jmeter -n -t performance/bibliotech-rendimiento.jmx -Jusuarios=50 -Jiteraciones=20 -Jrampa=10 -l resultados.jtl -e -o reporte
```

El reporte HTML queda en la carpeta `reporte`. Para probar el sistema desplegado se indican `-Jprotocol=https -Jhost=<dominio> -Jport=443`.

## Resultados (Docker Compose en un equipo local)

| Usuarios | Peticiones | Rendimiento | Media | Percentil 95 | Máximo | Errores |
|---|---|---|---|---|---|---|
| 10 | 611 | 60 /s | 9,1 ms | 15 ms | 184 ms | 0 % |
| 30 | 1 831 | 171 /s | 7,0 ms | 13 ms | 57 ms | 0 % |
| 50 | 3 051 | 281 /s | 6,3 ms | 11 ms | 70 ms | 0 % |
