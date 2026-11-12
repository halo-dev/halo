import type { Component } from "vue";

export type FilterDropdownValue = string | number | boolean;

export type FilterDropdownVariant = "default" | "outline";

/**
 * Spacing between the filters in a `VFilterDropdownGroup`. The values are
 * smaller than `VSpace` because filter triggers already have horizontal
 * padding.
 */
export type FilterDropdownGroupSpacing = "xs" | "sm" | "md" | "lg";

export interface FilterDropdownOption {
  /**
   * The text displayed in the option row and in the trigger.
   */
  label: string;
  /**
   * The value emitted when the option is selected. Options without a value are
   * treated as non-selectable nodes and can be used for tree parents.
   */
  value?: FilterDropdownValue;
  /**
   * Secondary text displayed next to the label.
   */
  description?: string;
  /**
   * Additional terms matched by the local search, such as a resource name.
   */
  keywords?: string[];
  /**
   * Optional leading icon component.
   */
  icon?: Component;
  /**
   * Whether the option can be selected.
   */
  disabled?: boolean;
  /**
   * Nested options rendered as a collapsible tree.
   */
  children?: FilterDropdownOption[];
  /**
   * Allow consumers to attach arbitrary data for custom option slots.
   */
  [key: string]: unknown;
}

export interface FilterDropdownGroup {
  /**
   * Optional heading displayed above the group.
   */
  label?: string;
  /**
   * Options in the group.
   */
  options: FilterDropdownOption[];
  [key: string]: unknown;
}

export type FilterDropdownItem = FilterDropdownOption | FilterDropdownGroup;
