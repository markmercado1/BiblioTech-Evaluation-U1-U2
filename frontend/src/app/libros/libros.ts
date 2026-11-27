import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Libro, LibroService } from '../services/libro';
import { mensajeDeError } from '../services/errores';

@Component({
  selector: 'app-libros',
  imports: [ReactiveFormsModule],
  templateUrl: './libros.html',
  styleUrl: './libros.css'
})
export class Libros implements OnInit {

  private readonly libroService = inject(LibroService);

  readonly libros = signal<Libro[]>([]);
  readonly mensaje = signal('');
  readonly registroCorrecto = signal<boolean | null>(null);
  readonly enviado = signal(false);

  // RN-01: ISBN, título y autor obligatorios; ejemplares mayores o iguales a 0.
  readonly formulario = new FormGroup({
    isbn: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)]
    }),
    titulo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)]
    }),
    autor: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)]
    }),
    ejemplaresDisponibles: new FormControl(1, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)]
    })
  });

  ngOnInit(): void {
    this.cargarLibros();
  }

  cargarLibros(): void {
    this.libroService.listar().subscribe({
      next: (libros) => this.libros.set(libros),
      error: (error) => {
        this.registroCorrecto.set(false);
        this.mensaje.set(mensajeDeError(error, 'No se pudo cargar el catálogo de libros.'));
      }
    });
  }

  /** Indica si un campo debe mostrar su error: tras intentar registrar o al tocarlo. */
  mostrarError(campo: 'isbn' | 'titulo' | 'autor' | 'ejemplaresDisponibles'): boolean {
    const control = this.formulario.controls[campo];
    return control.invalid && (this.enviado() || control.touched);
  }

  registrar(): void {
    this.enviado.set(true);
    this.mensaje.set('');
    this.registroCorrecto.set(null);

    if (this.formulario.invalid) {
      return;
    }

    this.libroService.registrar(this.formulario.getRawValue()).subscribe({
      next: () => {
        this.registroCorrecto.set(true);
        this.mensaje.set('Libro registrado correctamente.');
        this.formulario.reset({ isbn: '', titulo: '', autor: '', ejemplaresDisponibles: 1 });
        this.enviado.set(false);
        this.cargarLibros();
      },
      error: (error) => {
        this.registroCorrecto.set(false);
        this.mensaje.set(mensajeDeError(error, 'No se pudo registrar el libro.'));
      }
    });
  }
}
