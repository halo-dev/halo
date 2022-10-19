import type { Component } from "vue";

export type CoreMenuGroupId =
  | "dashboard"
  | "content"
  | "interface"
  | "system"
  | "tool";

export interface MenuGroupType {
  id: CoreMenuGroupId | string;
  name?: string;
  priority: number;
  items?: MenuItemType[];
}

export interface MenuItemType {
  name: string;
  path: string;
  mobile?: boolean;
  icon?: Component;
  meta?: Record<string, unknown>;
  children?: MenuItemType[];
}
