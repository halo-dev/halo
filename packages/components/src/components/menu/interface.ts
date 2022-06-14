import type { Component } from "vue";

export interface MenuGroupType {
  name?: string;
  items: MenuItemType[];
}

export interface MenuItemType {
  name: string;
  path: string;
  icon?: Component;
  meta?: Record<string, unknown>;
  children?: MenuItemType[];
}
