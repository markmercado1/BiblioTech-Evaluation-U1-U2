/// <reference types="cypress" />

/** Sufijo único para que los flujos se puedan repetir sin chocar con ISBN o DNI ya registrados. */
export function unico(): string {
  return `${Date.now()}${Math.floor(Math.random() * 1000)}`;
}

/** Crea un libro directamente en la API y devuelve su id. */
export function crearLibro(ejemplares: number, titulo: string, isbn: string): Cypress.Chainable<number> {
  return cy
    .request('POST', `${Cypress.expose('apiUrl')}/libros`, {
      isbn,
      titulo,
      autor: 'Autor E2E',
      ejemplaresDisponibles: ejemplares
    })
    .then((respuesta) => respuesta.body.id as number);
}

/** Crea un estudiante ACTIVO directamente en la API y devuelve su id. */
export function crearEstudiante(dni: string): Cypress.Chainable<number> {
  return cy
    .request('POST', `${Cypress.expose('apiUrl')}/estudiantes`, {
      dni,
      nombre: 'Estudiante E2E'
    })
    .then((respuesta) => respuesta.body.id as number);
}
