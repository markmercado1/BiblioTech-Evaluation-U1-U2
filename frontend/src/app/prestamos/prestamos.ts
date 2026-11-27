import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Prestamo, PrestamoActivo } from '../services/prestamo';
import { Libro, LibroService } from '../services/libro';
import { EstudianteService } from '../services/estudiante';
import { mensajeDeError } from '../services/errores';

@Component({
  selector: 'app-prestamos',
  imports: [FormsModule],
  templateUrl: './prestamos.html',
  styleUrl: './prestamos.css'
})
export class Prestamos implements OnInit {

  estudianteId: number = 1;
  libroId: number = 1;
  prestamoId: number | null = null;

  dni: string = '';
  nombre: string = '';

  readonly mensaje = signal('');
  readonly disponible = signal<boolean | null>(null);

  readonly mensajeEstudiante = signal('');
  readonly estudianteCorrecto = signal<boolean | null>(null);

  readonly prestamosActivos = signal<PrestamoActivo[]>([]);
  readonly consultaRealizada = signal(false);
  readonly libros = signal<Libro[]>([]);

  private readonly prestamoService = inject(Prestamo);
  private readonly libroService = inject(LibroService);
  private readonly estudianteService = inject(EstudianteService);

  ngOnInit(): void {
    this.cargarLibros();
  }

  cargarLibros(): void {
    this.libroService.listar().subscribe({
      next: (libros) => this.libros.set(libros),
      error: () => this.libros.set([])
    });
  }

  consultarPrestamosActivos(): void {
    this.prestamoService
      .obtenerPrestamosActivos(this.estudianteId)
      .subscribe({
        next: (prestamos) => {
          this.prestamosActivos.set(prestamos);
          this.consultaRealizada.set(true);
        },
        error: (error) => {
          this.prestamosActivos.set([]);
          this.consultaRealizada.set(false);
          this.mostrar(false, mensajeDeError(error, 'No se pudo consultar los préstamos activos.'));
        }
      });
  }

  validarPrestamo(): void {
    this.prestamoService
      .validarPrestamo(this.estudianteId, this.libroId)
      .subscribe({
        next: (resultado) => {
          this.mostrar(
            resultado,
            resultado ? 'El préstamo está disponible.' : 'El préstamo no puede realizarse.'
          );
        },
        error: (error) => {
          this.mostrar(false, mensajeDeError(error, 'Error al validar el préstamo.'));
        }
      });
  }

  /** RN-03: registra el préstamo y muestra el motivo si el backend lo rechaza. */
  registrarPrestamo(): void {
    this.prestamoService
      .registrarPrestamo(this.estudianteId, this.libroId)
      .subscribe({
        next: (prestamo) => {
          this.prestamoId = prestamo.id;
          this.mostrar(true, `Préstamo registrado correctamente (N° ${prestamo.id}).`);
          this.cargarLibros();
        },
        error: (error) => {
          this.mostrar(false, mensajeDeError(error, 'No se pudo registrar el préstamo.'));
        }
      });
  }

  /** RN-04: devuelve el libro y muestra el motivo si el backend lo rechaza. */
  devolverLibro(): void {
    if (this.prestamoId === null) {
      this.mostrar(false, 'Indica el número de préstamo a devolver.');
      return;
    }

    this.prestamoService
      .devolverLibro(this.prestamoId)
      .subscribe({
        next: () => {
          this.mostrar(true, `Préstamo ${this.prestamoId} devuelto correctamente.`);
          this.cargarLibros();
        },
        error: (error) => {
          this.mostrar(false, mensajeDeError(error, 'No se pudo devolver el libro.'));
        }
      });
  }

  /** RN-02: registra un estudiante nuevo con estado ACTIVO. */
  registrarEstudiante(): void {
    this.estudianteService
      .registrar({ dni: this.dni, nombre: this.nombre })
      .subscribe({
        next: (estudiante) => {
          this.estudianteCorrecto.set(true);
          this.mensajeEstudiante.set(
            `Estudiante registrado correctamente (ID ${estudiante.id}, estado ${estudiante.estado}).`
          );
          this.estudianteId = estudiante.id;
          this.dni = '';
          this.nombre = '';
        },
        error: (error) => {
          this.estudianteCorrecto.set(false);
          this.mensajeEstudiante.set(mensajeDeError(error, 'No se pudo registrar el estudiante.'));
        }
      });
  }

  private mostrar(correcto: boolean, texto: string): void {
    this.disponible.set(correcto);
    this.mensaje.set(texto);
  }
}
