import type { Component, Raw } from "vue";

/**
 * Defines a custom tab for the user detail page in the console.
 *
 * @remarks
 * User tabs allow plugins to extend user management interfaces with custom
 * functionality such as activity logs, custom user metadata, integration
 * settings, or additional user management tools.
 *
 * @example
 * ```typescript
 * const activityTab: UserTab = {
 *   id: 'activity-log',
 *   label: 'Activity Log',
 *   component: markRaw(ActivityLogComponent),
 *   priority: 10,
 *   permissions: ['system:users:view-activity']
 * };
 * ```
 */
export interface UserTab {
  /**
   * Unique identifier for the user tab.
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
   * The component typically receives the user object as a prop and should
   * implement user-related functionality or display user-specific information.
   */
  component: Raw<Component>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Priority for ordering multiple user tabs.
   * Higher priority tabs appear first (leftmost) in the tab navigation.
   */
  priority: number;
}

/**
 * Defines a custom tab for the user profile page in the user center.
 *
 * @remarks
 * User profile tabs allow plugins to extend the user-facing profile interface
 * with custom sections such as social connections, preferences, achievements,
 * subscription management, or personalization settings.
 *
 * @example
 * ```typescript
 * const preferencesTab: UserProfileTab = {
 *   id: 'preferences',
 *   label: 'Preferences',
 *   component: markRaw(PreferencesComponent),
 *   priority: 15
 * };
 * ```
 */
export interface UserProfileTab {
  /**
   * Unique identifier for the user profile tab.
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
   * The component should provide user-facing functionality that allows users
   * to view or modify their own profile information or settings.
   */
  component: Raw<Component>;

  /**
   * Optional array of permission identifiers required to view this tab.
   * The tab will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Priority for ordering multiple user profile tabs.
   * Higher priority tabs appear first (leftmost) in the tab navigation.
   */
  priority: number;
}
