// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface PluginDependencies {}

export interface License {
  name: string;
  url: string;
}

export interface Spec {
  displayName: string;
  version: string;
  author: string;
  logo: string;
  pluginDependencies: PluginDependencies;
  homepage: string;
  description: string;
  license: License[];
  requires: string;
  pluginClass: string;
  enabled: boolean;
}

export interface Metadata {
  name: string;
  version: number;
  creationTimestamp: Date;
}

export interface Status {
  phase: string;
  entry?: string;
  stylesheet?: string;
}

export interface Plugin {
  spec: Spec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  status: Status;
  extensions: Extension[];
}

export interface Extension {
  name: string;
  fields: string[];
}
