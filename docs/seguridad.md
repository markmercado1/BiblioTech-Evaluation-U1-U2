# Escaneo de dependencias

Herramienta: Docker Scout sobre la imagen del backend (`docker scout cves bibliotech-backend:v1`), que analiza el jar y la imagen base,
y `npm audit` sobre las dependencias del frontend.

## Backend: primer escaneo (Spring Boot 4.0.8)

12 vulnerabilidades (4 críticas, 5 altas, 3 medias) en 4 paquetes.

| Paquete | Versión | Vulnerabilidades | Gravedad | Acción | Resultado |
|---|---|---|---|---|---|
| `tomcat-embed-core` | 11.0.24 | CVE-2026-65905, CVE-2026-65637, CVE-2026-68525, CVE-2026-65182 (omisión de autenticación, validación de entrada, autorización y control de acceso) | 4 críticas (CVSS 9,1 a 9,8) | Subir Spring Boot de 4.0.8 a 4.0.10, que trae Tomcat 11.0.27 (corregido desde 11.0.25) | Corregido |
| `jackson-databind` | 3.1.5 | CVE-2026-91777, CVE-2026-91776, CVE-2026-68497 (consumo descontrolado de recursos); CVE-2026-83557, CVE-2026-19032 (deserialización y reflexión inseguras) | 3 altas, 2 medias | Misma actualización: Jackson 3.1.8 (corregido desde 3.1.7) | Corregido |
| `jackson-core` | 3.1.5 | CVE-2026-89425, CVE-2026-89407 (consumo de recursos y expresión regular ineficiente) | 2 altas | Misma actualización: Jackson 3.1.8 | Corregido |
| `stdlib` (biblioteca estándar de Go) | 1.27.3 | CVE-2026-97054 | 1 media | Viene de la imagen base de Java, no de BiblioTech (el Dockerfile solo agrega el jar). Se reconstruye la imagen con `--pull` para tomar la base más reciente y se mantiene en seguimiento hasta que la imagen base publique la corrección (1.27.4) | Aceptado, en seguimiento |

## Backend: escaneo posterior a la corrección (Spring Boot 4.0.10)

1 vulnerabilidad (media, `stdlib` de la imagen base). Las 75 pruebas del backend siguen en verde y la cobertura se mantiene.

## Frontend (`npm audit`, dependencias de producción)

| Paquete | Gravedad | Descripción | Acción |
|---|---|---|---|
| `@angular/router` 22.2.0 | Alta | Denegación de servicio en el renderizado del lado del servidor (SSR) | No es explotable: BiblioTech no usa SSR, nginx solo sirve archivos estáticos. Se actualiza con el siguiente parche de Angular 22 junto con el resto de paquetes `@angular/*` |
| `zod` 4.6.5 | Alta | Consumo de memoria al validar colecciones muy grandes | Dependencia de la herramienta `@angular/cli` (solo desarrollo); no forma parte del paquete que se despliega |

Al intentar corregirlos con `npm audit fix` ocurrió un error interno de npm; la actualización manual cambió más de mil líneas del archivo de bloqueo y se revirtió para no desestabilizar el frontend ya probado.
