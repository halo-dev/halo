import type { Component, Raw } from "vue";

export interface DashboardResponsiveLayout {
  lg?: DashboardWidget[];
  md?: DashboardWidget[];
  sm?: DashboardWidget[];
  xs?: DashboardWidget[];
  xxs?: DashboardWidget[];
}

export interface DashboardWidget {
  x: number;
  y: number;
  w: number;
  h: number;
  i: number | string;
  minW?: number;
  minH?: number;
  maxW?: number;
  maxH?: number;
  id: string;
  config?: Record<string, unknown>;
  permissions?: string[];
}

export interface DashboardWidgetDefinition {
  id: string;
  component: Raw<Component>;
  group: string;
  configFormKitSchema?:
    | Record<string, unknown>[]
    | (() => Promise<Record<string, unknown>[]>)
    | (() => Record<string, unknown>[]);
  defaultConfig?: Record<string, unknown>;
  defaultSize: {
    w: number;
    h: number;
    minW?: number;
    minH?: number;
    maxW?: number;
    maxH?: number;
  };
  permissions?: string[];
}

interface DashboardWidgetQuickActionBaseItem {
  id: string;
  permissions?: string[];
}

interface DashboardWidgetQuickActionComponentItem
  extends DashboardWidgetQuickActionBaseItem {
  component: Raw<Component>;
  icon?: Raw<Component>;
  title?: string;
  action?: () => void;
}

interface DashboardWidgetQuickActionStandardItem
  extends DashboardWidgetQuickActionBaseItem {
  component?: never;
  icon: Raw<Component>;
  title: string;
  action: () => void;
}

export type DashboardWidgetQuickActionItem =
  | DashboardWidgetQuickActionComponentItem
  | DashboardWidgetQuickActionStandardItem;
