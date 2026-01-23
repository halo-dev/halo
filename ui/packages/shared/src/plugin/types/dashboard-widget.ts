import type { Component, Raw } from "vue";
import type { RouteLocationRaw } from "vue-router";

/**
 * Defines responsive layout configurations for dashboard widgets across different screen sizes.
 *
 * @remarks
 * Each breakpoint can have a different layout arrangement. If a breakpoint is not specified,
 * it will fall back to the next larger breakpoint's layout.
 */
export interface DashboardResponsiveLayout {
  /**
   * Layout for large screens (typically ≥ 1200px).
   */
  lg?: DashboardWidget[];

  /**
   * Layout for medium screens (typically ≥ 996px).
   */
  md?: DashboardWidget[];

  /**
   * Layout for small screens (typically ≥ 768px).
   */
  sm?: DashboardWidget[];

  /**
   * Layout for extra small screens (typically ≥ 480px).
   */
  xs?: DashboardWidget[];

  /**
   * Layout for double extra small screens (typically < 480px).
   */
  xxs?: DashboardWidget[];
}

/**
 * Represents an instance of a dashboard widget with its position, size, and configuration.
 *
 * @remarks
 * Dashboard widgets are arranged in a grid layout system. The grid typically has 12 columns,
 * and widgets can span multiple rows and columns.
 */
export interface DashboardWidget {
  /**
   * The x-coordinate (column position) in the grid, starting from 0.
   */
  x: number;

  /**
   * The y-coordinate (row position) in the grid, starting from 0.
   */
  y: number;

  /**
   * The width of the widget in grid columns.
   */
  w: number;

  /**
   * The height of the widget in grid rows.
   */
  h: number;

  /**
   * Unique identifier for this widget instance in the layout.
   * Can be a number or string.
   */
  i: number | string;

  /**
   * Minimum width constraint in grid columns.
   */
  minW?: number;

  /**
   * Minimum height constraint in grid rows.
   */
  minH?: number;

  /**
   * Maximum width constraint in grid columns.
   */
  maxW?: number;

  /**
   * Maximum height constraint in grid rows.
   */
  maxH?: number;

  /**
   * Reference to the widget definition ID that this instance represents.
   */
  id: string;

  /**
   * User-specific configuration for this widget instance.
   * The structure depends on the widget's definition schema.
   */
  config?: Record<string, unknown>;

  /**
   * Optional array of permission identifiers required to view this widget instance.
   */
  permissions?: string[];
}

/**
 * Defines a dashboard widget type that can be added to the dashboard.
 *
 * @remarks
 * Widget definitions serve as templates from which users can create widget instances.
 * Each definition specifies the component, default configuration, and size constraints.
 *
 * @example
 * ```typescript
 * const widgetDef: DashboardWidgetDefinition = {
 *   id: 'plugin-name:widget-id',
 *   component: markRaw(MyWidgetComponent),
 *   group: 'statistics',
 *   defaultSize: { w: 4, h: 2, minW: 2, minH: 1 },
 *   defaultConfig: { refreshInterval: 60000 },
 *   configFormKitSchema: [
 *     { $formkit: 'number', name: 'refreshInterval', label: 'Refresh Interval (ms)' }
 *   ]
 * };
 * ```
 */
export interface DashboardWidgetDefinition {
  /**
   * Unique identifier for the widget definition.
   * Should follow a namespaced pattern (e.g., 'plugin-name:widget-id').
   */
  id: string;

  /**
   * The Vue component that renders the widget.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   */
  component: Raw<Component>;

  /**
   * Category or group for organizing widgets in the selection interface.
   * Common groups include 'statistics', 'content', 'system', etc.
   */
  group: string;

  /**
   * FormKit schema for the widget's configuration form.
   * Can be an array, a function returning an array, or a function returning a Promise.
   *
   * @remarks
   * Defines the editable settings that users can configure for widget instances.
   * The schema uses FormKit's schema format.
   */
  configFormKitSchema?:
    | Record<string, unknown>[]
    | (() => Promise<Record<string, unknown>[]>)
    | (() => Record<string, unknown>[]);

  /**
   * Default configuration values for new widget instances.
   * These values will be used when a user first adds the widget to their dashboard.
   */
  defaultConfig?: Record<string, unknown>;

  /**
   * Default size and constraints for the widget.
   *
   * @remarks
   * Specifies the initial dimensions and any size limitations when the widget is added.
   */
  defaultSize: {
    /**
     * Default width in grid columns.
     */
    w: number;

    /**
     * Default height in grid rows.
     */
    h: number;

    /**
     * Minimum width constraint in grid columns.
     */
    minW?: number;

    /**
     * Minimum height constraint in grid rows.
     */
    minH?: number;

    /**
     * Maximum width constraint in grid columns.
     */
    maxW?: number;

    /**
     * Maximum height constraint in grid rows.
     */
    maxH?: number;
  };

  /**
   * Optional array of permission identifiers required to add or view this widget.
   */
  permissions?: string[];
}

/**
 * Base interface for dashboard widget quick action items.
 */
interface DashboardWidgetQuickActionBaseItem {
  /**
   * Unique identifier for the quick action.
   */
  id: string;

  /**
   * Optional array of permission identifiers required to see this action.
   */
  permissions?: string[];
}

/**
 * A quick action item that uses a custom component for rendering.
 * Provides maximum flexibility for complex action UI.
 */
interface DashboardWidgetQuickActionComponentItem
  extends DashboardWidgetQuickActionBaseItem {
  /**
   * Custom Vue component for rendering the quick action.
   * When provided, standard properties (icon, title, action) are optional.
   */
  component: Raw<Component>;

  /**
   * Optional icon component displayed alongside the action.
   */
  icon?: Raw<Component>;

  /**
   * Optional title text for the action.
   */
  title?: string;

  /**
   * Optional click handler for the action.
   */
  action?: () => void;
}

/**
 * A standard quick action item with icon, title, and action handler.
 * Uses the default rendering style for consistency.
 */
interface DashboardWidgetQuickActionStandardItem
  extends DashboardWidgetQuickActionBaseItem {
  /**
   * Cannot provide a custom component for standard items.
   */
  component?: never;

  /**
   * Icon component displayed with the action (required for standard items).
   */
  icon: Raw<Component>;

  /**
   * Display title for the action (required for standard items).
   */
  title: string;

  /**
   * Click handler invoked when the action is triggered (required for standard items).
   */
  action: () => void;
}

/**
 * A quick action item that navigates to a route when triggered.
 */
interface DashboardWidgetQuickActionRouteItem
  extends DashboardWidgetQuickActionBaseItem {
  /**
   * Cannot provide a custom component for route items.
   */
  component?: never;

  /**
   * Cannot provide an action handler for route items.
   */
  action?: never;

  /**
   * Icon component displayed with the action (required for route items).
   */
  icon: Raw<Component>;

  /**
   * Display title for the action (required for route items).
   */
  title: string;

  /**
   * Route to navigate to when the action is triggered (required for route items).
   */
  route: RouteLocationRaw;
}

/**
 * Represents a quick action button that can be added to dashboard widgets.
 *
 * @remarks
 * Quick actions provide shortcuts to common operations directly from the dashboard.
 * They can either use a custom component for full control or follow the standard
 * pattern with an icon, title, and action handler.
 */
export type DashboardWidgetQuickActionItem =
  | DashboardWidgetQuickActionComponentItem
  | DashboardWidgetQuickActionStandardItem
  | DashboardWidgetQuickActionRouteItem;
