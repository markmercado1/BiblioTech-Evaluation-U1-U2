import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface NuevoEstudiante {
  dni: string;
  nombre: string;
}

export interface EstudianteRegistrado {
  id: number;
  dni: string;
  nombre: string;
  estado: string;
}

@Injectable({
  providedIn: 'root'
})
export class EstudianteService {

  private readonly apiUrl = '/api/estudiantes';
  private readonly http = inject(HttpClient);

  registrar(estudiante: NuevoEstudiante): Observable<EstudianteRegistrado> {
    return this.http.post<EstudianteRegistrado>(this.apiUrl, estudiante);
  }
}
