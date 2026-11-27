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
├── database/            (init.sql)
├── docs/                (estrategia de pruebas, capturas)
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
| `cd frontend && ng test --watch=false` | Pruebas Angular |
| `cd frontend && ng lint` | Análisis estático del frontend |
| `docker compose up -d --build` | Levanta base de datos, backend y frontend |

Las pruebas del backend leen la conexión de las variables `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` con el perfil `ci`.

## Variables de entorno

Copiar `.env.example` a `.env` y completar los valores. El archivo `.env` no se sube al repositorio.
