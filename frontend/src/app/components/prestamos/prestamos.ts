import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Prestamo, PrestamoActivo } from '../../services/prestamo';

@Component({
  selector: 'app-prestamos',
  imports: [FormsModule],
  templateUrl: './prestamos.html',
  styleUrl: './prestamos.css'
})
export class Prestamos {

  estudianteId: number = 1;
  libroId: number = 1;

  mensaje: string = '';
  disponible: boolean | null = null;

  private readonly prestamoService = inject(Prestamo);

  prestamosActivos: PrestamoActivo[] = [];
  consultaRealizada = false;

  consultarPrestamosActivos(): void {
    this.prestamoService
      .obtenerPrestamosActivos(this.estudianteId)
      .subscribe({
        next: (prestamos) => {
          this.prestamosActivos = prestamos;
          this.consultaRealizada = true;
        },
        error: () => {
          this.prestamosActivos = [];
          this.consultaRealizada = false;
          this.disponible = false;
          this.mensaje = 'No se pudo consultar los préstamos activos.';
        }
      });
  }

  validarPrestamo(): void {
    this.prestamoService
      .validarPrestamo(this.estudianteId, this.libroId)
      .subscribe({
        next: (resultado) => {
          this.disponible = resultado;

          this.mensaje = resultado ? 'El préstamo está disponible.' : 'El préstamo no puede realizarse.';
        },
        error: () => {
          this.disponible = false;
          this.mensaje = 'Error al validar el préstamo.';
        }
      });
  }

  registrarPrestamo(): void {
    this.prestamoService
      .registrarPrestamo(this.estudianteId, this.libroId)
      .subscribe({
        next: () => {
          this.disponible = true;
          this.mensaje = 'Préstamo registrado correctamente.';
        },
        error: () => {
          this.disponible = false;
          this.mensaje = 'No se pudo registrar el préstamo.';
        }
      });
  }
}