import type { Component } from "vue";
import type { ToastActionOptions, ToastStatus, ToastType } from "./types";

/**
 * Data attached to a Reka UI toast object and rendered by `Toast.vue`.
 * Internal to the toast component.
 */
export interface ToastItemData {
  type: ToastType;
  content: string;
  title?: string;
  count: number;
  /**
   * Normalized duration in milliseconds. `0` keeps the toast open.
   */
  duration: number;
  closable: boolean;
  frozenOnHover: boolean;
  icon?: Component;
  action?: ToastActionOptions;
  customClass?: string;
  status?: ToastStatus;
}
