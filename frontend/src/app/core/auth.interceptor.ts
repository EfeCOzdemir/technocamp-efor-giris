import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const header = auth.authHeader;
  const request =
    header && !req.headers.has('Authorization') ? req.clone({ setHeaders: { Authorization: header } }) : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Login denemesi dışındaki 401'ler oturumun geçersiz olduğu anlamına gelir.
      if (error.status === 401 && !req.headers.has('Authorization') && auth.user()) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};

/** Backend'in {"message": "..."} hata gövdesinden okunabilir mesaj çıkarır. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.error?.message) return error.error.message;
    if (error.status === 0) return 'Sunucuya ulaşılamıyor';
    if (error.status === 403) return 'Bu işlem için yetkiniz yok';
  }
  return 'Beklenmeyen bir hata oluştu';
}
