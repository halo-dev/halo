import type { Component, Raw } from "vue";

export interface EntityDropdownItem<T> {
  priority: number;
  component: Raw<Component>;
  props?: Record<string, unknown>;
  action?: (item?: T) => void;
  label?: string;
  visible?: boolean;
  permissions?: string[];
  children?: EntityDropdownItem<T>[];
}
