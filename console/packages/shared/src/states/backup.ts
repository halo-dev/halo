import type { Component, Raw } from "vue";

export interface BackupTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
}
