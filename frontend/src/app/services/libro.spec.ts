import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LibroService } from './libro';

describe('LibroService', () => {
  let servicio: LibroService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    servicio = TestBed.inject(LibroService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('GET: lista los libros del backend', () => {
    servicio.listar().subscribe(libros => {
      expect(libros.length).toBe(1);
      expect(libros[0].estado).toBe('DISPONIBLE');
    });

    const req = httpMock.expectOne('/api/libros');
    expect(req.request.method).toBe('GET');

    req.flush([{
      id: 1, isbn: 'ISBN-1', titulo: 'Clean Code', autor: 'Robert Martin',
      ejemplaresDisponibles: 2, estado: 'DISPONIBLE'
    }]);
  });

  it('POST: registra un libro enviando sus datos', () => {
    const nuevo = { isbn: 'ISBN-2', titulo: 'Refactoring', autor: 'Martin Fowler', ejemplaresDisponibles: 1 };

    servicio.registrar(nuevo).subscribe(libro => {
      expect(libro.id).toBe(2);
    });

    const req = httpMock.expectOne('/api/libros');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevo);

    req.flush({ ...nuevo, id: 2, estado: 'DISPONIBLE' });
  });

  it('POST: propaga el error 400 del backend', () => {
    let estado = 0;

    servicio.registrar({ isbn: '', titulo: 'X', autor: 'Y', ejemplaresDisponibles: 1 }).subscribe({
      error: (error) => (estado = error.status)
    });

    httpMock.expectOne('/api/libros')
      .flush({ mensaje: 'El ISBN es obligatorio' }, { status: 400, statusText: 'Bad Request' });

    expect(estado).toBe(400);
  });
});
