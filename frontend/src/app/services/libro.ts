import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Libro {
  id: number;
  isbn: string;
  titulo: string;
  autor: string;
  ejemplaresDisponibles: number;
  estado: string;
}

export interface NuevoLibro {
  isbn: string;
  titulo: string;
  autor: string;
  ejemplaresDisponibles: number;
}

@Injectable({
  providedIn: 'root'
})
export class LibroService {

  private readonly apiUrl = 'http://localhost:8080/api/libros';
  private readonly http = inject(HttpClient);

  listar(): Observable<Libro[]> {
    return this.http.get<Libro[]>(this.apiUrl);
  }

  registrar(libro: NuevoLibro): Observable<Libro> {
    return this.http.post<Libro>(this.apiUrl, libro);
  }
}
