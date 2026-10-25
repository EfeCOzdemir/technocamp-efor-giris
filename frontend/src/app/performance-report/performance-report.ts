import { Component, computed, effect, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ApiService } from '../core/api.service';
import { errorMessage } from '../core/auth.interceptor';
import { formatDate, formatDuration, netMinutes, toIsoDate } from '../core/date-utils';
import { AgentOption, Performance, ReportFilter } from '../core/models';

class TurkishPaginatorIntl extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Sayfa başına:';
  override nextPageLabel = 'Sonraki sayfa';
  override previousPageLabel = 'Önceki sayfa';
  override firstPageLabel = 'İlk sayfa';
  override lastPageLabel = 'Son sayfa';
  override getRangeLabel = (page: number, size: number, length: number) => {
    if (length === 0) return `0 / 0`;
    const start = page * size + 1;
    return `${start} – ${Math.min(start + size - 1, length)} / ${length}`;
  };
}

@Component({
  selector: 'app-performance-report',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  providers: [{ provide: MatPaginatorIntl, useClass: TurkishPaginatorIntl }],
  templateUrl: './performance-report.html',
  styleUrl: './performance-report.scss',
})
export class PerformanceReport {
  private readonly api = inject(ApiService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly formatDate = formatDate;
  protected readonly formatDuration = formatDuration;
  protected readonly netMinutes = netMinutes;

  readonly columns = ['fullName', 'agentId', 'dateInfo', 'beginTime', 'endTime', 'timeout', 'excuse', 'excuseHours'];

  readonly agents = signal<AgentOption[]>([]);
  readonly rows = signal<Performance[]>([]);
  readonly loading = signal(false);
  readonly downloading = signal(false);
  /** Tabloyu üreten (ve indirmede kullanılan) son uygulanmış filtre. */
  readonly applied = signal<ReportFilter>({});

  readonly dataSource = new MatTableDataSource<Performance>([]);
  readonly sort = viewChild(MatSort);
  readonly paginator = viewChild(MatPaginator);

  readonly totals = computed(() => {
    const rows = this.rows();
    return {
      count: rows.length,
      agents: new Set(rows.map((r) => r.agentId)).size,
      net: rows.reduce((sum, r) => sum + netMinutes(r), 0),
      excuse: rows.reduce((sum, r) => sum + r.excuseHours, 0),
    };
  });

  readonly filterForm = inject(FormBuilder).group({
    agentId: [null as string | null],
    from: [null as Date | null],
    to: [null as Date | null],
  });

  constructor() {
    this.dataSource.sortingDataAccessor = (row, column) => {
      switch (column) {
        case 'fullName':
          return `${row.firstName} ${row.surname}`.toLocaleLowerCase('tr-TR');
        case 'dateInfo':
          return `${row.dateInfo} ${row.beginTime}`;
        default:
          return (row as unknown as Record<string, string | number>)[column] ?? '';
      }
    };
    effect(() => {
      this.dataSource.sort = this.sort() ?? null;
      this.dataSource.paginator = this.paginator() ?? null;
    });
    this.api.agents().subscribe((agents) => this.agents.set(agents));
    this.search();
  }

  search(): void {
    const v = this.filterForm.getRawValue();
    const filter: ReportFilter = {
      agentId: v.agentId,
      from: v.from ? toIsoDate(v.from) : null,
      to: v.to ? toIsoDate(v.to) : null,
    };
    this.loading.set(true);
    this.api.report(filter).subscribe({
      next: (rows) => {
        this.applied.set(filter);
        this.rows.set(rows);
        this.dataSource.data = rows;
        this.paginator()?.firstPage();
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.snackBar.open(errorMessage(e), 'Kapat', { duration: 6000, panelClass: 'snack-error' });
      },
    });
  }

  clear(): void {
    this.filterForm.reset({ agentId: null, from: null, to: null });
    this.search();
  }

  download(): void {
    this.downloading.set(true);
    this.api.exportReport(this.applied()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `performans-raporu-${toIsoDate(new Date())}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
        this.downloading.set(false);
      },
      error: (e) => {
        this.downloading.set(false);
        this.snackBar.open(errorMessage(e), 'Kapat', { duration: 6000, panelClass: 'snack-error' });
      },
    });
  }

  appliedSummary(): string {
    const f = this.applied();
    const parts: string[] = [];
    if (f.agentId) {
      parts.push(this.agents().find((a) => a.agentId === f.agentId)?.fullName ?? f.agentId);
    }
    if (f.from || f.to) {
      parts.push(`${f.from ? formatDate(f.from) : '…'} – ${f.to ? formatDate(f.to) : '…'}`);
    }
    return parts.length ? parts.join(' · ') : 'Tüm temsilciler, tüm tarihler';
  }
}
