import type { Component, Raw } from "vue";

/**
 * Defines a custom operation (action button or menu item) for entity list items.
 *
 * @remarks
 * Operation items allow plugins to add custom actions to list rows across various
 * entity types (posts, pages, comments, etc.). Operations can be standalone buttons
 * or nested within dropdown menus, and can include permission checks and conditional visibility.
 *
 * @typeParam T - The type of entity this operation applies to (e.g., ListedPost, Plugin, Theme).
 *
 * @example
 * ```typescript
 * const exportOperation: OperationItem<ListedPost> = {
 *   priority: 20,
 *   component: markRaw(VButton),
 *   label: 'Export',
 *   action: (post) => {
 *     console.log('Exporting post:', post.metadata.name);
 *   },
 *   permissions: ['plugin:export:use']
 * };
 *
 * // Operation with children (dropdown menu)
 * const shareOperation: OperationItem<ListedPost> = {
 *   priority: 15,
 *   component: markRaw(VDropdown),
 *   label: 'Share',
 *   children: [
 *     {
 *       priority: 10,
 *       component: markRaw(VButton),
 *       label: 'Share to Twitter',
 *       action: (post) => { // ... }
 *     },
 *     {
 *       priority: 20,
 *       component: markRaw(VButton),
 *       label: 'Copy Link',
 *       action: (post) => { // ... }
 *     }
 *   ]
 * };
 * ```
 */
export interface OperationItem<T> {
  /**
   * Priority for ordering multiple operations.
   * Higher priority operations are displayed first (leftmost or at the top).
   */
  priority: number;

  /**
   * The Vue component that renders the operation.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   *
   * @remarks
   * Common components include buttons, dropdown menus, or custom action components.
   * The component receives props and should handle the visual representation and
   * interaction of the operation.
   */
  component: Raw<Component>;

  /**
   * Optional props to pass to the component.
   * Can be used to configure appearance, behavior, or additional data.
   */
  props?: Record<string, unknown>;

  /**
   * Optional action handler invoked when the operation is triggered.
   *
   * @param item - The entity instance that this operation was triggered on.
   *
   * @remarks
   * This is typically used for operations that need to perform actions on the entity,
   * such as exporting, sharing, or custom processing. The handler can be async.
   */
  action?: (item?: T) => void;

  /**
   * Optional display label for the operation.
   * Used for button text, menu items, or tooltips.
   */
  label?: string;

  /**
   * Whether the operation should be hidden by default.
   * Can be used to implement conditional visibility based on entity state or user context.
   */
  hidden?: boolean;

  /**
   * Optional array of permission identifiers required to see this operation.
   * The operation will only be visible to users with at least one of these permissions.
   */
  permissions?: string[];

  /**
   * Optional nested operations for creating hierarchical menus.
   *
   * @remarks
   * When provided, this operation becomes a dropdown menu or submenu containing
   * the child operations. Useful for grouping related actions together.
   */
  children?: OperationItem<T>[];
}
