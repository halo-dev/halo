import type { Component, Raw } from "vue";

export interface PluginInstallationTab {
  id: string;
  label: string;
  component: Raw<Component>;
  props?: Record<string, unknown>;
  permissions?: string[];
  priority: number;
}
