import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AgentOption, Performance, PerformanceRequest, ReportFilter } from './models';

function toParams(filter: ReportFilter): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(filter)) {
    if (value) params = params.set(key, value);
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  excuses(): Observable<string[]> {
    return this.http.get<string[]>('/api/excuses');
  }

  myPerformances(from: string, to: string): Observable<Performance[]> {
    return this.http.get<Performance[]>('/api/performances', { params: { from, to } });
  }

  create(request: PerformanceRequest): Observable<Performance> {
    return this.http.post<Performance>('/api/performances', request);
  }

  update(id: number, request: PerformanceRequest): Observable<Performance> {
    return this.http.put<Performance>(`/api/performances/${id}`, request);
  }

  delete(ids: number[]): Observable<void> {
    return this.http.delete<void>('/api/performances', { params: { ids: ids.join(',') } });
  }

  agents(): Observable<AgentOption[]> {
    return this.http.get<AgentOption[]>('/api/reports/agents');
  }

  report(filter: ReportFilter): Observable<Performance[]> {
    return this.http.get<Performance[]>('/api/reports/performances', { params: toParams(filter) });
  }

  exportReport(filter: ReportFilter): Observable<Blob> {
    return this.http.get('/api/reports/performances/export', { params: toParams(filter), responseType: 'blob' });
  }
}
