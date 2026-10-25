import { SelectionModel } from '@angular/cdk/collections';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { MatTooltipModule } from '@angular/material/tooltip';
import { filter, switchMap } from 'rxjs';
import { ApiService } from '../core/api.service';
import { errorMessage } from '../core/auth.interceptor';
import { ConfirmDialog, ConfirmData } from '../core/confirm-dialog';
import {
  addDays,
  formatDate,
  formatDuration,
  fromIsoDate,
  fromTime,
  netMinutes,
  startOfWeek,
  toIsoDate,
  toTime,
} from '../core/date-utils';
import { Performance, PerformanceRequest } from '../core/models';

@Component({
  selector: 'app-performance-entry',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatTimepickerModule,
    MatProgressBarModule,
  ],
  templateUrl: './performance-entry.html',
  styleUrl: './performance-entry.scss',
})
export class PerformanceEntry {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly formatDate = formatDate;
  protected readonly formatDuration = formatDuration;
  protected readonly netMinutes = netMinutes;

  readonly columns = ['select', 'dateInfo', 'beginTime', 'endTime', 'timeout', 'net', 'excuse', 'excuseHours', 'actions'];

  readonly weekStart = signal(startOfWeek(new Date()));
  readonly weekEnd = computed(() => addDays(this.weekStart(), 6));
  readonly isCurrentWeek = computed(() => toIsoDate(this.weekStart()) === toIsoDate(startOfWeek(new Date())));

  readonly rows = signal<Performance[]>([]);
  readonly excuses = signal<string[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly editing = signal<Performance | null>(null);

  readonly selection = new SelectionModel<number>(true, []);
  readonly selectedCount = signal(0);

  readonly totals = computed(() => {
    const rows = this.rows();
    return {
      days: new Set(rows.map((r) => r.dateInfo)).size,
      net: rows.reduce((sum, r) => sum + netMinutes(r), 0),
      timeout: rows.reduce((sum, r) => sum + r.timeout, 0),
      excuse: rows.reduce((sum, r) => sum + r.excuseHours, 0),
    };
  });

  readonly form = inject(FormBuilder).group({
    dateInfo: [new Date() as Date | null, Validators.required],
    beginTime: [fromTime('09:00') as Date | null, Validators.required],
    endTime: [fromTime('18:00') as Date | null, Validators.required],
    timeout: [1 as number | null, [Validators.required, Validators.min(0), Validators.max(24)]],
    excuse: [null as string | null],
    excuseHours: [{ value: 0 as number | null, disabled: true }, [Validators.min(1), Validators.max(24)]],
  });

  constructor() {
    this.selection.changed.subscribe(() => this.selectedCount.set(this.selection.selected.length));
    this.form.controls.excuse.valueChanges.subscribe((excuse) => {
      const hours = this.form.controls.excuseHours;
      if (excuse) {
        hours.enable({ emitEvent: false });
        if (!hours.value) hours.setValue(1);
      } else {
        hours.setValue(0);
        hours.disable({ emitEvent: false });
      }
    });
    this.api.excuses().subscribe((list) => this.excuses.set(list));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.myPerformances(toIsoDate(this.weekStart()), toIsoDate(this.weekEnd())).subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.selection.clear();
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.notify(errorMessage(e), true);
      },
    });
  }

  shiftWeek(weeks: number): void {
    this.weekStart.set(addDays(this.weekStart(), weeks * 7));
    this.load();
  }

  goToCurrentWeek(): void {
    this.weekStart.set(startOfWeek(new Date()));
    this.load();
  }

  weekLabel(): string {
    return `${formatDate(toIsoDate(this.weekStart()))} – ${formatDate(toIsoDate(this.weekEnd()))}`;
  }

  // --- Seçim ---
  isAllSelected(): boolean {
    return this.rows().length > 0 && this.selectedCount() === this.rows().length;
  }

  toggleAll(): void {
    if (this.isAllSelected()) this.selection.clear();
    else this.selection.select(...this.rows().map((r) => r.id));
  }

  // --- Ekle / Düzenle ---
  startEdit(row: Performance): void {
    this.editing.set(row);
    this.form.setValue({
      dateInfo: fromIsoDate(row.dateInfo),
      beginTime: fromTime(row.beginTime),
      endTime: fromTime(row.endTime),
      timeout: row.timeout,
      excuse: row.excuse,
      excuseHours: row.excuseHours,
    });
    document.querySelector('.entry-form')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  cancelEdit(): void {
    this.editing.set(null);
    this.resetForm();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const request: PerformanceRequest = {
      dateInfo: toIsoDate(v.dateInfo!),
      beginTime: toTime(v.beginTime!),
      endTime: toTime(v.endTime!),
      timeout: v.timeout ?? 0,
      excuse: v.excuse || null,
      excuseHours: v.excuse ? (v.excuseHours ?? 0) : 0,
    };
    if (request.endTime <= request.beginTime) {
      this.notify('Mesai bitiş saati başlangıç saatinden sonra olmalıdır', true);
      return;
    }

    const editing = this.editing();
    this.saving.set(true);
    const call = editing ? this.api.update(editing.id, request) : this.api.create(request);
    call.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.notify(editing ? 'Kayıt güncellendi' : 'Kayıt eklendi');
        this.editing.set(null);
        this.resetForm();
        // Kaydedilen gün başka bir haftadaysa o haftaya geç
        const savedWeek = startOfWeek(fromIsoDate(saved.dateInfo));
        if (toIsoDate(savedWeek) !== toIsoDate(this.weekStart())) this.weekStart.set(savedWeek);
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.notify(errorMessage(e), true);
      },
    });
  }

  // --- Silme ---
  deleteOne(row: Performance): void {
    this.confirmAndDelete([row.id], {
      title: 'Kaydı sil',
      message: `${formatDate(row.dateInfo)} tarihli kayıt silinsin mi?`,
    });
  }

  deleteSelected(): void {
    const ids = [...this.selection.selected];
    this.confirmAndDelete(ids, {
      title: 'Seçili kayıtları sil',
      message: `${ids.length} kayıt kalıcı olarak silinecek. Emin misiniz?`,
    });
  }

  private confirmAndDelete(ids: number[], data: ConfirmData): void {
    this.dialog
      .open(ConfirmDialog, { data, width: '400px' })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.delete(ids)),
      )
      .subscribe({
        next: () => {
          if (this.editing() && ids.includes(this.editing()!.id)) this.cancelEdit();
          this.notify(ids.length > 1 ? `${ids.length} kayıt silindi` : 'Kayıt silindi');
          this.load();
        },
        error: (e) => this.notify(errorMessage(e), true),
      });
  }

  private resetForm(): void {
    this.form.reset({
      dateInfo: new Date(),
      beginTime: fromTime('09:00'),
      endTime: fromTime('18:00'),
      timeout: 1,
      excuse: null,
      excuseHours: 0,
    });
  }

  private notify(message: string, isError = false): void {
    this.snackBar.open(message, 'Kapat', { duration: isError ? 6000 : 3000, panelClass: isError ? 'snack-error' : '' });
  }
}
