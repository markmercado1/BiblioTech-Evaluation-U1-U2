# Política de ramas y revisión de código

BiblioTech sigue Git Flow simplificado. El equipo son dos integrantes y cada cambio lo revisa el otro.

## Ramas

| Rama | Para qué sirve | Quién la actualiza |
|---|---|---|
| `main` | Versión estable lista para producción. Cada versión (`v1.0.0`, `v1.0.1`...) se etiqueta aquí. | Solo por pull request desde `develop` (o `hotfix/*`) |
| `develop` | Integración: reúne lo ya revisado y probado de cada funcionalidad. | Solo por pull request desde `feature/*` |
| `feature/<tema>` | Una funcionalidad o tarea concreta. Se crea desde `develop` y se borra al unirla. | Quien la desarrolla |
| `hotfix/<tema>` | Corrección urgente de producción. Se crea desde `main` y vuelve a `main` y a `develop`. | Quien la desarrolla |

Nombres: minúsculas y guiones, por ejemplo `feature/frontend-pantallas` o `feature/e2e-postman`.

## Reglas

1. **Nadie hace push directo a `main` ni a `develop`.** Todo cambio entra con un pull request.
2. **Cada pull request apunta a `develop`**, salvo el de publicación (`develop` → `main`) y los `hotfix/*`.
3. **El autor no aprueba su propio pull request.** Lo revisa el otro integrante y deja al menos un comentario útil (qué revisó o qué sugiere).
4. **El pull request solo se une si el CI está en verde:** compilación, pruebas de la Unidad 1 (`mvn verify` con JaCoCo ≥ 80 %, `ng test`, `ng lint`), construcción de los contenedores y quality gate de SonarCloud.
5. **Mensajes de commit** en español, en infinitivo y con el cambio concreto: «Agregar pantalla de libros», «Corregir el orden de las reglas de RN-03».
6. **Un pull request, un tema.** Si mezcla temas distintos, se divide.
7. **Se borra la rama `feature/*` al unirla.**

## Flujo de trabajo

```
feature/x  ──PR + revisión──►  develop  ──PR + revisión──►  main  ──etiqueta vX.Y.Z──►  imágenes y despliegue
```

1. `git switch develop` y `git pull`.
2. `git switch -c feature/<tema>`, desarrollar y hacer commits pequeños.
3. `git push -u origin feature/<tema>` y abrir el pull request hacia `develop`.
4. El CI y SonarCloud se ejecutan solos. Si fallan, se corrige en la misma rama.
5. El compañero revisa con la lista de abajo, comenta y aprueba.
6. Se une el pull request y se borra la rama.
7. Para publicar una versión se abre un pull request `develop` → `main`, se revisa y se etiqueta el resultado.

## Criterios para aprobar un pull request

- [ ] Hace lo que dice su descripción y nada más.
- [ ] El código es legible y mantiene la estructura del proyecto (controlador, servicio, repositorio, modelo).
- [ ] Tiene pruebas para lo nuevo y las existentes siguen en verde.
- [ ] No incluye contraseñas, tokens ni archivos `.env`.
- [ ] El CI y el quality gate de SonarCloud están en verde.
- [ ] La descripción y los comentarios del código se entienden sin preguntar al autor.

## Protección de ramas (configuración en GitHub)

En **Settings → Branches → Add branch ruleset** (o *branch protection rule*), para `main` y para `develop`:

- Exigir un pull request antes de unir, con **1 aprobación** como mínimo.
- Descartar las aprobaciones anteriores cuando se suben cambios nuevos.
- Exigir que pasen los controles de estado: `Backend - Maven, JUnit y JaCoCo`, `Frontend - ESLint, pruebas y build`, `Contenedores - Docker Compose` y `SonarCloud Code Analysis`.
- Exigir que la rama esté actualizada antes de unir.
- Bloquear los push forzados y el borrado de la rama.

## Secretos del repositorio

Las contraseñas y los tokens viven solo en **Settings → Secrets and variables → Actions**. El repositorio no contiene ningún valor secreto; `.env.example` solo trae nombres de variables.

| Secreto | Uso |
|---|---|
| `SONAR_TOKEN` | Análisis de SonarCloud |
| `CI_DB_PASSWORD` | Contraseña de la base de datos efímera que usa el CI |
