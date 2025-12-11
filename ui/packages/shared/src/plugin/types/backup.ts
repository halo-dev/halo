import type { Component, Raw } from "vue";

/**
 * Defines a custom tab for the backup management page.
 *
 * @remarks
 * Backup tabs allow plugins to extend the backup interface with custom
 * functionality such as cloud backup providers, automated backup schedules,
 * or specialized restore operations.
 */
export interface BackupTab {
  /**
   * Unique identifier for the backup tab.
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
   */
  component: Raw<Component>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];
}
