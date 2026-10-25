const pad = (n: number) => String(n).padStart(2, '0');

/** Yerel saat dilimine göre yyyy-MM-dd (toISOString UTC'ye kaydırdığı için kullanılmıyor). */
export function toIsoDate(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function fromIsoDate(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
}

/** yyyy-MM-dd -> dd.MM.yyyy */
export function formatDate(iso: string): string {
  const [y, m, d] = iso.split('-');
  return `${d}.${m}.${y}`;
}

export function toTime(date: Date): string {
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function fromTime(time: string): Date {
  const [h, m] = time.split(':').map(Number);
  const date = new Date();
  date.setHours(h, m, 0, 0);
  return date;
}

export function minutesBetween(begin: string, end: string): number {
  const [bh, bm] = begin.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  return eh * 60 + em - (bh * 60 + bm);
}

/** Mesai süresinden mola düşülmüş net çalışma (dakika). */
export function netMinutes(p: { beginTime: string; endTime: string; timeout: number }): number {
  return Math.max(0, minutesBetween(p.beginTime, p.endTime) - p.timeout * 60);
}

export function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m ? `${h} sa ${m} dk` : `${h} sa`;
}

export function startOfWeek(date: Date): Date {
  const d = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const day = (d.getDay() + 6) % 7; // Pazartesi = 0
  d.setDate(d.getDate() - day);
  return d;
}

export function addDays(date: Date, days: number): Date {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}
