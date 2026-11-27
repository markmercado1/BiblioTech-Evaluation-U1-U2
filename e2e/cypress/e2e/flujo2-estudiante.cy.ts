import { unico } from '../support/e2e';

describe('Flujo 2: registrar un estudiante', () => {

  it('registra un estudiante nuevo y queda con estado ACTIVO', () => {
    const dni = `DNI-${unico()}`;

    cy.visit('/prestamos');

    cy.get('#dni').type(dni);
    cy.get('#nombre').type('Ana Quispe');
    cy.get('#btnRegistrarEstudiante').click();

    cy.get('#resultadoEstudiante')
      .should('contain', 'Estudiante registrado correctamente')
      .and('contain', 'ACTIVO');
  });

  it('rechaza un DNI repetido y muestra el motivo', () => {
    const dni = `DNI-${unico()}`;

    cy.visit('/prestamos');

    for (const nombre of ['Ana Quispe', 'Luis Mamani']) {
      cy.get('#dni').type(dni);
      cy.get('#nombre').type(nombre);
      cy.get('#btnRegistrarEstudiante').click();
    }

    cy.get('#resultadoEstudiante').should('contain', 'El DNI ya está registrado');
  });
});
