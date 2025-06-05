import type { Component } from "vue";

export interface QuickActionItemDefinition {
  id: string;
  icon?: Component;
  component?: Component;
  title?: string;
  action?: () => void;
  permissions?: string[];
}
