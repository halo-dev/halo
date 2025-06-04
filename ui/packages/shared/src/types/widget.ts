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
  name: string;
  componentName: string;
  config?: Record<string, unknown>;
  permissions?: string[];
}

export interface DashboardWidgetDefinition {
  name: string;
  componentName: string;
  group: string;
  configFormKitSchema?: Record<string, unknown>[];
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
