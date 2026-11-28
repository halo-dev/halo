import type { Component } from "vue";

export type SegmentedControlSize = "sm" | "md";
export type SegmentedControlDirection = "row" | "column";
export type SegmentedControlValue = string | number;

export interface SegmentedControlItem {
  value: SegmentedControlValue;
  label?: string;
  icon?: Component;
  disabled?: boolean;
  ariaLabel?: string;
  title?: string;
}
