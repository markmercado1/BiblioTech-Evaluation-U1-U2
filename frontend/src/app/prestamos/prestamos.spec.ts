import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Prestamos } from './prestamos';
import { Prestamo } from '../services/prestamo';
import { LibroService } from '../services/libro';
import { EstudianteService } from '../services/estudiante';

describe('Prestamos (pantalla 2)', () => {
  let fixture: ComponentFixture<Prestamos>;
  let componente: Prestamos;

  const prestamoServicio = {
    validarPrestamo: vi.fn(),
    obtenerPrestamosActivos: vi.fn(),
    registrarPrestamo: vi.fn(),
    devolverLibro: vi.fn()
  };
  const libroServicio = { listar: vi.fn(), registrar: vi.fn() };
  const estudianteServicio = { registrar: vi.fn() };

  const pagina = () => fixture.nativeElement as HTMLElement;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  const rechazo = (mensaje: string) =>
    throwError(() => new HttpErrorResponse({ status: 400, error: { mensaje } }));

  beforeEach(async () => {
    vi.resetAllMocks();
    libroServicio.listar.mockReturnValue(of([
      { id: 1, isbn: 'ISBN-1', titulo: 'Clean Code', autor: 'Robert Martin', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' }
    ]));

    await TestBed.configureTestingModule({
      imports: [Prestamos],
      providers: [
        { provide: Prestamo, useValue: prestamoServicio },
        { provide: LibroService, useValue: libroServicio },
        { provide: EstudianteService, useValue: estudianteServicio }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Prestamos);
    componente = fixture.componentInstance;
    await renderizar();
  });

  it('should create', () => {
    expect(componente).toBeTruthy();
  });

  it('muestra los libros con sus ejemplares', () => {
    const filas = pagina().querySelectorAll('[data-cy="fila-libro"]');

    expect(filas.length).toBe(1);
    expect(filas[0].querySelector('[data-cy="ejemplares"]')?.textContent).toContain('2');
  });

  it('RN-03: al registrar un préstamo muestra el mensaje y recarga los ejemplares', async () => {
    prestamoServicio.registrarPrestamo.mockReturnValue(of({ id: 7, estado: 'ACTIVO' }));

    componente.registrarPrestamo();
    await renderizar();

    expect(pagina().querySelector('#resultado')?.textContent).toContain('Préstamo registrado correctamente');
    expect(componente.prestamoId).toBe(7);
    expect(libroServicio.listar).toHaveBeenCalledTimes(2);
  });

  it('RN-03: si el backend rechaza el préstamo muestra el motivo', async () => {
    prestamoServicio.registrarPrestamo.mockReturnValue(rechazo('El libro no tiene ejemplares disponibles'));

    componente.registrarPrestamo();
    await renderizar();

    const resultado = pagina().querySelector('#resultado');
    expect(resultado?.textContent).toContain('El libro no tiene ejemplares disponibles');
    expect(resultado?.classList).toContain('error');
  });

  it('RN-04: devuelve el libro y muestra el mensaje de éxito', async () => {
    prestamoServicio.devolverLibro.mockReturnValue(of({ id: 7, estado: 'DEVUELTO' }));
    componente.prestamoId = 7;

    componente.devolverLibro();
    await renderizar();

    expect(prestamoServicio.devolverLibro).toHaveBeenCalledWith(7);
    expect(pagina().querySelector('#resultado')?.textContent).toContain('devuelto correctamente');
  });

  it('RN-04: si el préstamo ya fue devuelto muestra el motivo del backend', async () => {
    prestamoServicio.devolverLibro.mockReturnValue(rechazo('El préstamo ya fue devuelto'));
    componente.prestamoId = 7;

    componente.devolverLibro();
    await renderizar();

    expect(pagina().querySelector('#resultado')?.textContent).toContain('El préstamo ya fue devuelto');
  });

  it('RN-04: sin número de préstamo no llama al backend', async () => {
    componente.prestamoId = null;

    componente.devolverLibro();
    await renderizar();

    expect(prestamoServicio.devolverLibro).not.toHaveBeenCalled();
    expect(pagina().querySelector('#resultado')?.textContent).toContain('Indica el número de préstamo');
  });

  it('RN-02: registra un estudiante y muestra que quedó ACTIVO', async () => {
    estudianteServicio.registrar.mockReturnValue(of({ id: 9, dni: '70000009', nombre: 'Ana', estado: 'ACTIVO' }));
    componente.dni = '70000009';
    componente.nombre = 'Ana';

    componente.registrarEstudiante();
    await renderizar();

    expect(estudianteServicio.registrar).toHaveBeenCalledWith({ dni: '70000009', nombre: 'Ana' });
    expect(pagina().querySelector('#resultadoEstudiante')?.textContent).toContain('ACTIVO');
    expect(componente.estudianteId).toBe(9);
  });

  it('RN-02: si el DNI está repetido muestra el motivo del backend', async () => {
    estudianteServicio.registrar.mockReturnValue(rechazo('El DNI ya está registrado'));

    componente.registrarEstudiante();
    await renderizar();

    expect(pagina().querySelector('#resultadoEstudiante')?.textContent).toContain('El DNI ya está registrado');
  });
});
