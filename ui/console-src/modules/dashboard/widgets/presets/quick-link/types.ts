import type { Component } from "vue";

export interface QuickLinkItemDefinition {
  id: string;
  icon?: Component;
  component?: Component;
  title?: string;
  action?: () => void;
  permissions?: string[];
}
