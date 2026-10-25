import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/guards';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./login/login').then((m) => m.Login), title: 'Giriş · Efor Takip' },
  {
    path: '',
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'performans-girisi' },
      {
        path: 'performans-girisi',
        loadComponent: () => import('./performance-entry/performance-entry').then((m) => m.PerformanceEntry),
        title: 'Performans Girişi · Efor Takip',
      },
      {
        path: 'performans-raporu',
        loadComponent: () => import('./performance-report/performance-report').then((m) => m.PerformanceReport),
        canActivate: [adminGuard],
        title: 'Performans Raporu · Efor Takip',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
