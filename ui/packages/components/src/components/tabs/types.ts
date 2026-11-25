import type { Component } from "vue";

export type TabsType = "default" | "pills" | "outline";
export type TabsDirection = "row" | "column";

export interface TabbarItem {
  id?: string | number;
  label?: string;
  icon?: Component;
  disabled?: boolean;
  badge?: string | number;
  ariaLabel?: string;
  [key: string]: unknown;
}
