import { crearEstudiante, crearLibro, unico } from '../support/e2e';

describe('Flujo 3: prestar un libro y ver bajar sus ejemplares', () => {

  it('al prestar baja un ejemplar y al devolver se restituye', () => {
    const sufijo = unico();
    const titulo = `Prestamo E2E ${sufijo}`;

    crearLibro(2, titulo, `ISBN-${sufijo}`).then((libroId) => {
      crearEstudiante(`DNI-${sufijo}`).then((estudianteId) => {
        cy.visit('/prestamos');

        cy.contains('[data-cy=fila-libro]', titulo)
          .find('[data-cy=ejemplares]').should('have.text', '2');

        cy.get('#estudianteId').clear().type(String(estudianteId));
        cy.get('#libroId').clear().type(String(libroId));
        cy.get('#btnRegistrar').click();

        cy.get('#resultado').should('contain', 'Préstamo registrado correctamente');
        cy.contains('[data-cy=fila-libro]', titulo)
          .find('[data-cy=ejemplares]').should('have.text', '1');

        cy.get('#btnDevolver').click();

        cy.get('#resultado').should('contain', 'devuelto correctamente');
        cy.contains('[data-cy=fila-libro]', titulo)
          .find('[data-cy=ejemplares]').should('have.text', '2');
      });
    });
  });

  it('rechaza el préstamo de un libro sin ejemplares y muestra el motivo', () => {
    const sufijo = unico();

    crearLibro(0, `Agotado E2E ${sufijo}`, `ISBN-${sufijo}`).then((libroId) => {
      crearEstudiante(`DNI-${sufijo}`).then((estudianteId) => {
        cy.visit('/prestamos');

        cy.get('#estudianteId').clear().type(String(estudianteId));
        cy.get('#libroId').clear().type(String(libroId));
        cy.get('#btnRegistrar').click();

        cy.get('#resultado')
          .should('contain', 'El libro no tiene ejemplares disponibles')
          .and('have.class', 'error');
      });
    });
  });

  it('rechaza el cuarto préstamo activo del mismo estudiante', () => {
    const sufijo = unico();

    crearLibro(10, `Maximo E2E ${sufijo}`, `ISBN-${sufijo}`).then((libroId) => {
      crearEstudiante(`DNI-${sufijo}`).then((estudianteId) => {
        cy.visit('/prestamos');

        cy.get('#estudianteId').clear().type(String(estudianteId));
        cy.get('#libroId').clear().type(String(libroId));

        for (let i = 0; i < 3; i++) {
          cy.get('#btnRegistrar').click();
          cy.get('#resultado').should('contain', 'Préstamo registrado correctamente');
        }

        cy.get('#btnRegistrar').click();
        cy.get('#resultado').should('contain', 'máximo de 3 préstamos activos');
      });
    });
  });
});
