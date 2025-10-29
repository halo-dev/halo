import type { Component, Raw } from "vue";

/**
 * Defines a custom tab for the theme list page.
 *
 * @remarks
 * Theme list tabs allow plugins to extend the theme management interface with
 * custom functionality such as theme marketplaces, remote theme browsers,
 * theme backup/restore tools, or advanced theme customization interfaces.
 *
 * @example
 * ```typescript
 * const themeMarketplaceTab: ThemeListTab = {
 *   id: 'marketplace',
 *   label: 'Theme Marketplace',
 *   component: markRaw(MarketplaceComponent),
 *   priority: 20,
 *   permissions: ['plugin:theme-marketplace:view']
 * };
 * ```
 */
export interface ThemeListTab {
  /**
   * Unique identifier for the theme list tab.
   * Should follow a namespaced pattern to avoid conflicts (e.g., 'plugin-name:tab-id').
   */
  id: string;

  /**
   * Display label for the tab.
   * This text will be shown in the tab navigation.
   */
  label: string;

  /**
   * The Vue component that renders the tab content.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   *
   * @remarks
   * The component should implement the theme-related functionality and integrate
   * appropriately with the theme management system.
   */
  component: Raw<Component>;

  /**
   * Optional props to pass to the component.
   * Can be used to configure behavior or provide additional context.
   */
  props?: Record<string, unknown>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Priority for ordering multiple theme list tabs.
   * Higher priority tabs appear first (leftmost) in the tab navigation.
   */
  priority: number;
}
