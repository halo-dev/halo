import type { Component, Raw } from "vue";

/**
 * Defines a custom field (column) that can be added to entity list views.
 *
 * @remarks
 * Entity field items allow plugins to add custom columns or metadata displays
 * to list views such as posts, pages, comments, etc. Fields can be positioned
 * at the start or end of the list and can include custom rendering logic.
 *
 * @example
 * ```typescript
 * const customField: EntityFieldItem = {
 *   priority: 10,
 *   position: 'end',
 *   component: markRaw(CustomFieldComponent),
 *   props: { format: 'short' },
 *   permissions: ['plugin:my-plugin:view']
 * };
 * ```
 */
export interface EntityFieldItem {
  /**
   * Priority for ordering multiple custom fields.
   * Higher priority fields are displayed first within their position group.
   */
  priority: number;

  /**
   * The position where this field should be inserted in the list.
   * - 'start': Before the default fields
   * - 'end': After the default fields
   */
  position: "start" | "end";

  /**
   * The Vue component that renders the field content.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   *
   * @remarks
   * The component typically receives the entity object as a prop and should
   * render the custom field information in a list-friendly format.
   */
  component: Raw<Component>;

  /**
   * Optional props to pass to the component.
   * These can be used to configure the field's behavior or appearance.
   */
  props?: Record<string, unknown>;

  /**
   * Optional array of permission identifiers required to view this field.
   * The field will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Whether the field should be hidden by default.
   * Can be used to implement toggleable columns or conditional visibility.
   */
  hidden?: boolean;
}
