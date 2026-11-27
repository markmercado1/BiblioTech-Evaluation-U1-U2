# BiblioTech

Sistema de gestión de biblioteca: backend Spring Boot, frontend Angular y base de datos PostgreSQL.
Proyecto de las Unidades 1 (estrategia de pruebas) y 2 (CI/CD y despliegue) del curso
Pruebas y Despliegue de Software, Universidad Peruana Unión.

## Integrantes

- Mark Emerson Mercado Huaracha
- Cristian Mamani Morales

## Estructura

```
bibliotech/
├── .github/workflows/   (ci.yml, sonar.yml, publish.yml)
├── backend/             (Spring Boot 4, Java 21)
│   └── src/test/java/upeu/bibliotech_v2/
│       ├── unitarias/   (JUnit 5 con patrón AAA y camino básico)
│       ├── mockito/     (pruebas con @Mock / @InjectMocks)
│       └── integracion/ (@WebMvcTest y @SpringBootTest)
├── frontend/            (Angular 22)
│   └── src/app/
│       ├── libros/      (pantalla 1: catálogo de libros, ruta /libros)
│       ├── prestamos/   (pantalla 2: estudiantes y préstamos, ruta /prestamos)
│       └── services/    (LibroService, EstudianteService, Prestamo)
├── e2e/                 (Cypress: 3 flujos)
├── postman/             (colección y entorno para Newman)
├── database/            (init.sql)
├── docs/                (camino básico, estrategia de pruebas, capturas)
├── docker-compose.yml
└── .env.example
```

## Requisitos

- Java 21 y Maven 3.9 (o el wrapper `mvnw`)
- Node.js 22.22.3 o superior y npm
- PostgreSQL 17 (o Docker)

## Comandos

| Comando | Qué hace |
|---|---|
| `cd backend && mvn test` | Pruebas del backend |
| `cd backend && mvn verify` | Pruebas del backend, reporte JaCoCo (`backend/target/site/jacoco/index.html`) y verificación de cobertura mínima del 80 % |
| `cd frontend && npm ci` | Instala dependencias del frontend |
| `cd frontend && ng test --watch=false` | Pruebas Angular, con reporte de cobertura (en esta versión de Angular el indicador es `--coverage` y ya viene activo por defecto) |
| `cd frontend && ng lint` | Análisis estático del frontend |
| `cd e2e && npm ci && npx cypress run` | Flujos E2E con el backend y el frontend levantados |
| `newman run postman/coleccion.json -e postman/entorno.json` | Pruebas de la API desde consola, con el backend levantado |
| `docker compose up -d --build` | Levanta base de datos, backend y frontend |

Las pruebas del backend leen la conexión de las variables `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` con el perfil `ci`.

## Ejecutar las pruebas E2E y de API en local

1. Crear una base de datos vacía (por ejemplo `bibliotech_e2e`) en PostgreSQL.
2. Backend: `cd backend && mvn -DskipTests package`, luego
   `java -jar target/bibliotech_v2-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:postgresql://localhost:5432/bibliotech_e2e`.
3. Frontend: `cd frontend && npx ng serve` (http://localhost:4200).
4. En otra terminal: `cd e2e && npx cypress run` y `newman run postman/coleccion.json -e postman/entorno.json`.

Los flujos de Cypress y la colección de Postman crean sus propios datos con valores únicos,
por lo que se pueden repetir sin limpiar la base.

## Integración continua

El workflow `ci.yml` se ejecuta en cada push y pull request hacia `main` y `develop`:

| Job | Qué hace |
|---|---|
| Backend | `mvn verify`: compila, ejecuta las pruebas de la Unidad 1 y exige JaCoCo ≥ 80 %; caché de Maven |
| Frontend | `ng lint`, `ng test` con cobertura y `ng build`; caché de npm |
| Contenedores | `docker compose up -d --build` y comprobación de que el backend y el frontend responden |

`sonar.yml` envía el código y la cobertura a SonarCloud y aplica el quality gate.
La política de ramas, la revisión de pull requests y los secretos están en [docs/politica-ramas.md](docs/politica-ramas.md).

## Entrega y despliegue

`cd.yml` se ejecuta al integrar en `main`: publica las imágenes `bibliotech-u2-backend` y `bibliotech-u2-frontend` en el
registro de GitHub con la versión del archivo `VERSION`, crea la etiqueta `vX.Y.Z` y despliega con comprobación por HTTPS.
También permite volver a una versión anterior (rollback) ejecutándolo a mano con esa versión.
Ver [docs/despliegue.md](docs/despliegue.md) y [docs/estrategia-entrega.md](docs/estrategia-entrega.md).

## Perfiles y variables de entorno

El backend tiene los perfiles `dev` (por defecto) y `prod`, elegidos con `SPRING_PROFILES_ACTIVE`.
**Ningún archivo del repositorio contiene contraseñas**: la contraseña de la base de datos se lee de
`SPRING_DATASOURCE_PASSWORD`. Para ejecutar el backend o sus pruebas fuera de Docker hay que definir esa variable
(y, si hace falta, `SPRING_DATASOURCE_URL` y `SPRING_DATASOURCE_USERNAME`).

Con Docker Compose, copiar `.env.example` a `.env` y completar los valores. El archivo `.env` no se sube al repositorio.
`docker compose up -d --build` falla con un mensaje claro si falta alguna variable.
En desarrollo con `ng serve`, las llamadas a `/api` se reenvían al backend en `localhost:8080` mediante `proxy.conf.json`.
