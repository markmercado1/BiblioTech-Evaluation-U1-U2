# Despliegue continuo

## Pipeline: del commit a producción

```
feature/*  ──PR revisado──►  develop  ──PR revisado──►  main
                                │                          │
                           ci.yml + sonar.yml         ci.yml + sonar.yml
                    (compila, prueba, analiza,      (los mismos controles)
                     construye los contenedores)            │
                                                            ▼
                                                        cd.yml
                              1. lee la versión (archivo VERSION)
                              2. construye y publica las imágenes en ghcr.io
                                 con la etiqueta X.Y.Z, latest y sha-<commit>
                              3. crea la etiqueta de Git vX.Y.Z
                              4. pide el despliegue de esa versión
                              5. comprueba el sistema por HTTPS
```

| Etapa | Herramienta |
|---|---|
| Control de versiones y revisión | Git, GitHub, pull requests con revisión |
| Compilar y probar | Maven + JUnit + JaCoCo, Angular + Vitest, GitHub Actions (`ci.yml`) |
| Análisis estático | SonarCloud (`sonar.yml`) y ESLint |
| Empaquetado | Docker (Dockerfile multi-stage) y Docker Compose |
| Registro de imágenes | GitHub Container Registry (`ghcr.io`) |
| Despliegue | GitHub Actions (`cd.yml`) + hosting con HTTPS |

## Versionado

Se usa versionado semántico (`MAJOR.MINOR.PATCH`). La versión vigente está en el archivo `VERSION` de la raíz.
Para publicar una versión nueva, el pull request `develop` → `main` cambia ese archivo (por ejemplo de `1.0.0` a `1.1.0`).
Al integrarlo en `main`, `cd.yml` publica `bibliotech-backend` y `bibliotech-frontend` con tres etiquetas:

| Etiqueta | Para qué sirve |
|---|---|
| `1.1.0` | Versión inmutable; es la que se despliega y a la que se vuelve en un rollback |
| `latest` | Última versión publicada |
| `sha-<commit>` | Trazabilidad exacta hasta el commit |

## Perfiles de Spring

| Perfil | Uso | Diferencias |
|---|---|---|
| `dev` (por defecto) | Desarrollo local | Muestra el SQL, registros en nivel DEBUG |
| `prod` | Producción | Sin SQL en los registros, errores sin detalles internos, reconoce el proxy HTTPS |
| `ci` | Pruebas automáticas | Base de datos efímera con datos de prueba |

Se elige con la variable `SPRING_PROFILES_ACTIVE`. Ningún perfil contiene contraseñas: la conexión
se lee de `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

## Secretos y variables

Nada secreto está en el repositorio. Los valores viven en **Settings → Secrets and variables → Actions**
y en el panel del hosting.

| Nombre | Tipo | Dónde | Uso |
|---|---|---|---|
| `SONAR_TOKEN` | Secreto | GitHub | Análisis de SonarCloud |
| `CI_DB_PASSWORD` | Secreto | GitHub | Base de datos efímera del CI |
| `DEPLOY_HOOK_BACKEND` | Secreto | GitHub | URL que ordena al hosting desplegar el backend |
| `DEPLOY_HOOK_FRONTEND` | Secreto | GitHub | URL que ordena al hosting desplegar el frontend |
| `APP_URL` | Variable | GitHub | Dirección HTTPS pública del sistema |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Entorno | Hosting (backend) | Conexión a la base de datos |
| `SPRING_PROFILES_ACTIVE=prod` | Entorno | Hosting (backend) | Perfil de producción |
| `BACKEND_URL` | Entorno | Hosting (frontend) | URL HTTPS del backend al que el frontend reenvía `/api` |

Si faltan `DEPLOY_HOOK_*` o `APP_URL`, `cd.yml` publica las imágenes igualmente y avisa de que omite el despliegue.

## Configuración del hosting (una sola vez)

Los pasos describen un hosting de contenedores con HTTPS automático (por ejemplo Render).

1. **Hacer públicas las imágenes.** Tras la primera ejecución de `cd.yml`, en GitHub: perfil → *Packages* → `bibliotech-backend` →
   *Package settings* → *Change visibility* → Public. Repetir con `bibliotech-frontend`. (Alternativa: dar al hosting un token de lectura del registro.)
2. **Base de datos.** Crear una base PostgreSQL en el hosting y anotar el servidor, el nombre, el usuario y la contraseña.
3. **Backend.** Crear un servicio web desde una imagen existente: `ghcr.io/markmercado1/bibliotech-backend:latest`.
   Variables de entorno: `SPRING_PROFILES_ACTIVE=prod` y las tres `SPRING_DATASOURCE_*`
   (`jdbc:postgresql://<servidor>:5432/<base>`). Ruta de comprobación de salud: `/api/libros`.
4. **Frontend.** Crear otro servicio web desde `ghcr.io/markmercado1/bibliotech-frontend:latest` con la variable
   `BACKEND_URL` igual a la URL HTTPS pública del backend. El frontend llama a `/api` y nginx reenvía la petición al backend, de modo que el navegador solo habla con una dirección HTTPS.
5. **Despliegue automático.** Copiar la URL del *Deploy Hook* de cada servicio a los secretos `DEPLOY_HOOK_BACKEND` y `DEPLOY_HOOK_FRONTEND`,
   y la dirección HTTPS del frontend a la variable `APP_URL`.

## Cómo comprobar un despliegue

- Pestaña **Actions → BiblioTech CD**: los jobs *Publicar*, *Crear la etiqueta* y *Desplegar* en verde.
- Abrir `APP_URL`: debe cargar con candado HTTPS y mostrar la lista de libros (el frontend consume al backend desplegado).
- `docker pull ghcr.io/markmercado1/bibliotech-backend:<versión>` confirma que la imagen existe con su versión.
