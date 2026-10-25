export type Role = 'AGENT' | 'ADMIN';

export interface CurrentUser {
  username: string;
  agentId: string;
  firstName: string;
  surname: string;
  role: Role;
}

export interface Performance {
  id: number;
  agentId: string;
  firstName: string;
  surname: string;
  /** yyyy-MM-dd */
  dateInfo: string;
  /** HH:mm */
  beginTime: string;
  /** HH:mm */
  endTime: string;
  timeout: number;
  excuse: string | null;
  excuseHours: number;
}

export interface PerformanceRequest {
  dateInfo: string;
  beginTime: string;
  endTime: string;
  timeout: number;
  excuse: string | null;
  excuseHours: number;
}

export interface AgentOption {
  agentId: string;
  fullName: string;
}

export interface ReportFilter {
  agentId?: string | null;
  from?: string | null;
  to?: string | null;
}
