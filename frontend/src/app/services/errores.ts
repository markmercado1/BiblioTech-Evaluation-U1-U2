import { HttpErrorResponse } from '@angular/common/http';

/** Devuelve el mensaje que envía el backend ({"mensaje": "..."}) o uno por defecto. */
export function mensajeDeError(error: unknown, porDefecto: string): string {
  if (error instanceof HttpErrorResponse && typeof error.error?.mensaje === 'string') {
    return error.error.mensaje;
  }
  return porDefecto;
}
