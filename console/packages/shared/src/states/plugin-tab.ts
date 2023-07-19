import type { Component, Raw } from "vue";

export interface PluginTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
}
