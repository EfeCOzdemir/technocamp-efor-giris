import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { CurrentUser } from './models';

const STORAGE_KEY = 'efor.auth';

/**
 * HTTP Basic kimlik doğrulama. Kimlik bilgisi yalnızca sekme kapanana kadar
 * sessionStorage'da tutulur ve interceptor tarafından her isteğe eklenir.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private token: string | null = sessionStorage.getItem(STORAGE_KEY);
  readonly user = signal<CurrentUser | null>(null);
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');

  get authHeader(): string | null {
    return this.token ? `Basic ${this.token}` : null;
  }

  login(username: string, password: string): Observable<CurrentUser> {
    const token = btoa(String.fromCharCode(...new TextEncoder().encode(`${username}:${password}`)));
    const headers = new HttpHeaders({ Authorization: `Basic ${token}` });
    return this.http.get<CurrentUser>('/api/auth/me', { headers }).pipe(
      tap((user) => {
        this.token = token;
        sessionStorage.setItem(STORAGE_KEY, token);
        this.user.set(user);
      }),
    );
  }

  /** Sayfa yenilendiğinde kayıtlı kimlik bilgisiyle kullanıcıyı geri yükler. */
  restore(): Observable<boolean> {
    if (this.user()) return of(true);
    if (!this.token) return of(false);
    return this.http.get<CurrentUser>('/api/auth/me').pipe(
      tap((user) => this.user.set(user)),
      map(() => true),
      catchError(() => {
        this.clear();
        return of(false);
      }),
    );
  }

  logout(): void {
    this.clear();
    this.router.navigateByUrl('/login');
  }

  private clear(): void {
    this.token = null;
    sessionStorage.removeItem(STORAGE_KEY);
    this.user.set(null);
  }
}
