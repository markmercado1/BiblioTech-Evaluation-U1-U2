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
Al integrarlo en `main`, `cd.yml` publica `bibliotech-u2-backend` y `bibliotech-u2-frontend` con tres etiquetas:

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

## Configuración del hosting en Render (una sola vez)

Render ofrece HTTPS automático, despliegue desde imágenes de un registro y un plan gratuito. Se entra con la cuenta de GitHub en https://render.com.
Orden recomendado: primero se publican las imágenes (el pull request `develop` → `main` lanza `cd.yml`) y después se crean los servicios.

1. **Hacer públicas las imágenes.** En GitHub: perfil → *Packages* → `bibliotech-u2-backend` → *Package settings* → *Change visibility* → Public.
   Repetir con `bibliotech-u2-frontend`. (Alternativa: registrar en Render un token de lectura del registro.)
2. **Base de datos.** *New → PostgreSQL*, nombre `bibliotech-db`, plan **Free**. Al crearse, anotar el *Hostname* interno, el *Database*, el *Username* y el *Password*.
3. **Backend.** *New → Web Service → Existing Image*, imagen `ghcr.io/markmercado1/bibliotech-u2-backend:latest`, nombre `bibliotech-backend`, la misma región que la base de datos y plan **Free**.
   Variables de entorno:

   | Variable | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<hostname interno>:5432/<database>` |
   | `SPRING_DATASOURCE_USERNAME` | el *Username* de la base |
   | `SPRING_DATASOURCE_PASSWORD` | el *Password* de la base |
   | `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=65` (limita la memoria de Java al plan gratuito) |

   En *Advanced* → *Health Check Path*: `/api/libros`. Render asigna el puerto con `PORT`, que el backend ya lee.
4. **Frontend.** *New → Web Service → Existing Image*, imagen `ghcr.io/markmercado1/bibliotech-u2-frontend:latest`, nombre `bibliotech-frontend`, plan **Free**.
   Variable `BACKEND_URL` con la URL HTTPS que Render dio al backend (por ejemplo `https://bibliotech-backend.onrender.com`, sin barra final).
   El frontend llama a `/api` y nginx reenvía la petición al backend: el navegador solo habla con una dirección HTTPS.
5. **Despliegue automático.** En cada servicio: *Settings → Deploy Hook*, copiar la URL.
   En GitHub (*Settings → Secrets and variables → Actions*): secretos `DEPLOY_HOOK_BACKEND` y `DEPLOY_HOOK_FRONTEND` con esas URL,
   y la variable `APP_URL` con la dirección HTTPS del frontend. Después, ejecutar *BiblioTech CD* a mano (*Run workflow*, sin versión).

`cd.yml` agrega `imgURL=<imagen:versión>` al *deploy hook*, así Render despliega exactamente la versión publicada y no una etiqueta móvil.
La imagen indicada en el servicio de Render debe tener el mismo registro y repositorio que las que publica el workflow; solo cambia la etiqueta.

**Limitaciones del plan gratuito.**
- Los servicios se duermen tras 15 minutos sin visitas y tardan cerca de un minuto en despertar con la primera petición. La comprobación de `cd.yml` espera hasta 6 minutos por eso.
- La base de datos gratuita expira a los 30 días y tiene 1 GB. Basta para la demostración; si expira se crea otra y se actualizan las tres variables `SPRING_DATASOURCE_*`.
- La base de producción empieza vacía (Hibernate crea las tablas al arrancar); los datos se cargan desde la propia aplicación.
## Cómo comprobar un despliegue

- Pestaña **Actions → BiblioTech CD**: los jobs *Publicar*, *Crear la etiqueta* y *Desplegar* en verde.
- Abrir `APP_URL`: debe cargar con candado HTTPS y mostrar la lista de libros (el frontend consume al backend desplegado).
- `docker pull ghcr.io/markmercado1/bibliotech-u2-backend:<versión>` confirma que la imagen existe con su versión.
