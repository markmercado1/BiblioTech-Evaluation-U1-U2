import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EstudianteService } from './estudiante';

describe('EstudianteService', () => {
  let servicio: EstudianteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    servicio = TestBed.inject(EstudianteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('POST: registra un estudiante y recibe el estado ACTIVO', () => {
    servicio.registrar({ dni: '70000001', nombre: 'Ana' }).subscribe(estudiante => {
      expect(estudiante.estado).toBe('ACTIVO');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/estudiantes');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ dni: '70000001', nombre: 'Ana' });

    req.flush({ id: 1, dni: '70000001', nombre: 'Ana', estado: 'ACTIVO' });
  });
});
