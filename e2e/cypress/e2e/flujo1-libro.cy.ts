import { unico } from '../support/e2e';

describe('Flujo 1: registrar un libro y verlo en la lista', () => {

  it('muestra los errores cuando faltan el ISBN, el título y el autor', () => {
    cy.visit('/libros');

    cy.get('#btnRegistrarLibro').click();

    cy.get('#errorIsbn').should('contain', 'El ISBN es obligatorio');
    cy.get('#errorTitulo').should('contain', 'El título es obligatorio');
    cy.get('#errorAutor').should('contain', 'El autor es obligatorio');
  });

  it('muestra el error cuando los ejemplares son negativos', () => {
    cy.visit('/libros');

    cy.get('#ejemplares').clear().type('-1');
    cy.get('#btnRegistrarLibro').click();

    cy.get('#errorEjemplares').should('contain', 'no pueden ser negativos');
  });

  it('registra un libro nuevo y aparece en la lista con su estado', () => {
    const sufijo = unico();
    const titulo = `Libro E2E ${sufijo}`;

    cy.visit('/libros');

    cy.get('#isbn').type(`ISBN-${sufijo}`);
    cy.get('#titulo').type(titulo);
    cy.get('#autor').type('Autor E2E');
    cy.get('#ejemplares').clear().type('3');
    cy.get('#btnRegistrarLibro').click();

    cy.get('#resultadoLibro').should('contain', 'Libro registrado correctamente');

    cy.contains('[data-cy=fila-libro]', titulo).within(() => {
      cy.get('[data-cy=ejemplares]').should('have.text', '3');
      cy.contains('DISPONIBLE');
    });
  });

  it('rechaza un ISBN repetido y muestra el motivo', () => {
    const sufijo = unico();

    cy.visit('/libros');

    for (const titulo of [`Original ${sufijo}`, `Copia ${sufijo}`]) {
      cy.get('#isbn').type(`ISBN-${sufijo}`);
      cy.get('#titulo').type(titulo);
      cy.get('#autor').type('Autor E2E');
      cy.get('#btnRegistrarLibro').click();
    }

    cy.get('#resultadoLibro').should('contain', 'El ISBN ya está registrado');
  });
});
