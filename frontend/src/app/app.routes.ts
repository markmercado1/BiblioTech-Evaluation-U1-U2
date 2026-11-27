import { Routes } from '@angular/router';
import { Libros } from './libros/libros';
import { Prestamos } from './prestamos/prestamos';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'prestamos' },
  { path: 'libros', component: Libros },
  { path: 'prestamos', component: Prestamos },
  { path: '**', redirectTo: 'prestamos' }
];
