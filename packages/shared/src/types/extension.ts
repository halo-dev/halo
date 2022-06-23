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
