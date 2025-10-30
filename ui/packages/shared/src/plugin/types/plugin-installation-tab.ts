import type { Component, Raw } from "vue";

/**
 * Defines a custom tab for the plugin installation page.
 *
 * @remarks
 * Plugin installation tabs allow plugins to provide alternative installation methods
 * or additional functionality during the plugin installation process. Examples include
 * installing from a URL, importing from a marketplace, or batch installation features.
 *
 * @example
 * ```typescript
 * const remoteInstallTab: PluginInstallationTab = {
 *   id: 'remote-install',
 *   label: 'Install from URL',
 *   component: markRaw(RemoteInstallComponent),
 *   priority: 10,
 *   permissions: ['plugin:install:remote']
 * };
 * ```
 */
export interface PluginInstallationTab {
  /**
   * Unique identifier for the installation tab.
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
   * The component should implement the installation interface and handle
   * the complete installation workflow, including validation and error handling.
   */
  component: Raw<Component>;

  /**
   * Optional props to pass to the component.
   * Can be used to configure the installation behavior or provide additional context.
   */
  props?: Record<string, unknown>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Priority for ordering multiple installation tabs.
   * Higher priority tabs appear first (leftmost) in the tab navigation.
   */
  priority: number;
}
