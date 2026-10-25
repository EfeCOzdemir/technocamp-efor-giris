import { describe, expect, it } from 'vitest';
import { formatDate, formatDuration, netMinutes, startOfWeek, toIsoDate } from './date-utils';

describe('date-utils', () => {
  it('formats ISO dates in Turkish order', () => {
    expect(formatDate('2026-10-20')).toBe('20.10.2026');
  });

  it('finds Monday as the start of the week', () => {
    expect(toIsoDate(startOfWeek(new Date(2026, 9, 25)))).toBe('2026-10-19'); // Pazar
    expect(toIsoDate(startOfWeek(new Date(2026, 9, 19)))).toBe('2026-10-19'); // Pazartesi
  });

  it('subtracts break time from shift length', () => {
    expect(netMinutes({ beginTime: '08:30', endTime: '17:45', timeout: 1 })).toBe(495);
    expect(formatDuration(495)).toBe('8 sa 15 dk');
    expect(formatDuration(480)).toBe('8 sa');
  });
});
