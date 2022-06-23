export interface License {
  name?: string;
  url?: string;
}

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

export interface Plugin {
  spec: PluginSpec;
  status?: PluginStatus;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  extensions: Extension[];
}

export interface Extension {
  name: string;
  fields: string[];
}

export interface PluginSpec {
  displayName?: string;
  version?: string;
  author?: string;
  logo?: string;
  pluginDependencies?: {
    [key: string]: string;
  };
  homepage?: string;
  description?: string;
  license?: License[];
  requires?: string;
  pluginClass?: string;
  enabled?: boolean;
}

export interface PluginStatus {
  phase?:
    | "CREATED"
    | "DISABLED"
    | "RESOLVED"
    | "STARTED"
    | "STOPPED"
    | "FAILED";
  reason?: string;
  message?: string;
  lastStartTime?: string;
  lastTransitionTime?: string;
  entry?: string;
  stylesheet?: string;
}

export interface PersonalAccessToken {
  spec?: PersonalAccessTokenSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface PersonalAccessTokenSpec {
  userName?: string;
  displayName?: string;
  revoked?: boolean;
  expiresAt?: string;
  scopes?: string;
  tokenDigest?: string;
}

export interface RoleBinding {
  subjects?: Subject[];
  roleRef?: RoleRef;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface RoleRef {
  kind?: string;
  name?: string;
  apiGroup?: string;
}

export interface Subject {
  kind?: string;
  name?: string;
  apiGroup?: string;
}

export interface PolicyRule {
  apiGroups?: string[];
  resources?: string[];
  resourceNames?: string[];
  nonResourceURLs?: string[];
  verbs?: string[];
  pluginName?: string;
}

export interface Role {
  rules?: PolicyRule[];
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

export interface FileReverseProxyProvider {
  directory?: string;
  filename?: string;
}

export interface ReverseProxy {
  rules?: ReverseProxyRule[];
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface ReverseProxyRule {
  path?: string;
  file?: FileReverseProxyProvider;
}
