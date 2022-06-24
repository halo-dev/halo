export interface Metadata {
  name: string;
  labels?: {
    [key: string]: string | null;
  } | null;
  annotations?: {
    [key: string]: string | null;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface Extension<T> {
  spec?: T;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface LoginHistory {
  loginAt: string;
  sourceIp: string;
  userAgent: string;
  successful: boolean;
  reason?: string;
}

export interface User {
  spec: UserSpec;
  status?: UserStatus;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface UserSpec {
  displayName: string;
  avatar?: string;
  email: string;
  phone?: string;
  password?: string;
  bio?: string;
  registeredAt?: string;
  twoFactorAuthEnabled?: boolean;
  disabled?: boolean;
  loginHistoryLimit?: number;
}

export interface UserStatus {
  lastLoginAt?: string;
  loginHistories?: LoginHistory[];
}
