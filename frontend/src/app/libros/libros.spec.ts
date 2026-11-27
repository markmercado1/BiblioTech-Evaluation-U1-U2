import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Libros } from './libros';
import { Libro, LibroService } from '../services/libro';

const LIBROS: Libro[] = [
  { id: 1, isbn: '978-0-00-000001-1', titulo: 'Clean Code', autor: 'Robert Martin', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' },
  { id: 2, isbn: '978-0-00-000002-8', titulo: 'Refactoring', autor: 'Martin Fowler', ejemplaresDisponibles: 0, estado: 'NO DISPONIBLE' }
];

describe('Libros (pantalla 1)', () => {
  let fixture: ComponentFixture<Libros>;
  let componente: Libros;
  let servicio: { listar: ReturnType<typeof vi.fn>; registrar: ReturnType<typeof vi.fn> };

  const pagina = () => fixture.nativeElement as HTMLElement;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  function completarFormulario(valores: Partial<Record<'isbn' | 'titulo' | 'autor' | 'ejemplaresDisponibles', string | number>>): void {
    componente.formulario.setValue({
      isbn: '978-0-00-000003-5',
      titulo: 'Domain-Driven Design',
      autor: 'Eric Evans',
      ejemplaresDisponibles: 3,
      ...valores
    } as { isbn: string; titulo: string; autor: string; ejemplaresDisponibles: number });
  }

  beforeEach(async () => {
    servicio = {
      listar: vi.fn().mockReturnValue(of(LIBROS)),
      registrar: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Libros],
      providers: [{ provide: LibroService, useValue: servicio }]
    }).compileComponents();

    fixture = TestBed.createComponent(Libros);
    componente = fixture.componentInstance;
    await renderizar();
  });

  it('muestra en la lista los libros del backend con su estado', () => {
    const filas = pagina().querySelectorAll('[data-cy="fila-libro"]');

    expect(filas.length).toBe(2);
    expect(filas[0].textContent).toContain('Clean Code');
    expect(filas[0].textContent).toContain('DISPONIBLE');
    expect(filas[1].textContent).toContain('NO DISPONIBLE');
  });

  it('exige el ISBN: con ISBN vacío muestra el error y no registra', async () => {
    completarFormulario({ isbn: '' });

    componente.registrar();
    await renderizar();

    expect(componente.formulario.controls.isbn.errors?.['required']).toBeTruthy();
    expect(pagina().querySelector('#errorIsbn')?.textContent).toContain('El ISBN es obligatorio');
    expect(servicio.registrar).not.toHaveBeenCalled();
  });

  it('no acepta ejemplares negativos y muestra el error', async () => {
    completarFormulario({ ejemplaresDisponibles: -1 });

    componente.registrar();
    await renderizar();

    expect(componente.formulario.controls.ejemplaresDisponibles.errors?.['min']).toBeTruthy();
    expect(pagina().querySelector('#errorEjemplares')?.textContent).toContain('no pueden ser negativos');
    expect(servicio.registrar).not.toHaveBeenCalled();
  });

  it('exige título y autor aunque solo tengan espacios', async () => {
    completarFormulario({ titulo: '   ', autor: '' });

    componente.registrar();
    await renderizar();

    expect(pagina().querySelector('#errorTitulo')).not.toBeNull();
    expect(pagina().querySelector('#errorAutor')).not.toBeNull();
  });

  it('con datos válidos registra el libro, limpia el formulario y recarga la lista', async () => {
    servicio.registrar.mockReturnValue(of({ ...LIBROS[0], id: 3 }));
    completarFormulario({});

    componente.registrar();
    await renderizar();

    expect(servicio.registrar).toHaveBeenCalledWith({
      isbn: '978-0-00-000003-5',
      titulo: 'Domain-Driven Design',
      autor: 'Eric Evans',
      ejemplaresDisponibles: 3
    });
    expect(pagina().querySelector('#resultadoLibro')?.textContent).toContain('Libro registrado correctamente');
    expect(componente.formulario.controls.isbn.value).toBe('');
    expect(servicio.listar).toHaveBeenCalledTimes(2);
  });

  it('muestra el mensaje del backend cuando rechaza el libro (ISBN repetido)', async () => {
    servicio.registrar.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 400, error: { mensaje: 'El ISBN ya está registrado' } })));
    completarFormulario({});

    componente.registrar();
    await renderizar();

    expect(pagina().querySelector('#resultadoLibro')?.textContent).toContain('El ISBN ya está registrado');
  });
});
