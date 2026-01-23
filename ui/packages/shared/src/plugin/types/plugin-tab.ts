import type { Component, Raw } from "vue";

/**
 * Defines a custom tab for a plugin's own detail/settings page.
 *
 * @remarks
 * Plugin tabs allow plugins to organize their settings, configuration, and
 * management interfaces into multiple sections. This is useful for plugins
 * with extensive configuration options or multiple functional areas.
 *
 * @example
 * ```typescript
 * const settingsTab: PluginTab = {
 *   id: 'settings',
 *   label: 'Settings',
 *   component: markRaw(SettingsComponent),
 *   permissions: ['plugin:my-plugin:manage']
 * };
 * ```
 */
export interface PluginTab {
  /**
   * Unique identifier for the plugin tab.
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
   * The component should implement the tab's functionality, which might include
   * configuration forms, status displays, or management interfaces.
   */
  component: Raw<Component>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];
}
