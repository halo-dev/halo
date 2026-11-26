import type { Component, VNode } from "vue";

export type ToastType = "success" | "info" | "warning" | "error";

export type ToastStatus = "loading" | "success" | "error";

export type ToastPlacement =
  | "top-left"
  | "top-center"
  | "top-right"
  | "bottom-left"
  | "bottom-center"
  | "bottom-right";

export interface ToastActionOptions {
  label: string;
  /**
   * Describes the action for screen reader users. Defaults to `label`.
   */
  altText?: string;
  /**
   * Whether the toast should close when the action is clicked.
   * @defaultValue true
   */
  closeOnClick?: boolean;
  onClick?: (event: MouseEvent) => void;
}

export interface ToastProps {
  type?: ToastType;
  content?: string;
  /**
   * Time in milliseconds before the toast closes. `0` or a negative value
   * keeps the toast open until it is dismissed.
   */
  duration?: number;
  closable?: boolean;
  /**
   * Kept for backwards compatibility. Toasts are now always paused while the
   * pointer or keyboard focus is inside the toast viewport (or the window is
   * blurred), so `false` no longer disables that behaviour.
   */
  frozenOnHover?: boolean;
  count?: number;
  onClose?: () => void;
  /**
   * Optional heading rendered above the content.
   */
  title?: string;
  /**
   * Overrides the icon derived from `type`.
   */
  icon?: Component;
  /**
   * Optional action button rendered below the content.
   */
  action?: ToastActionOptions;
  /**
   * Extra class names applied to the toast element.
   */
  customClass?: string;
  /**
   * Stable identifier used to update an existing toast instead of adding a new one.
   */
  key?: string;
  /**
   * Merge toasts with the same content and type, showing how many times the
   * message was repeated.
   * @defaultValue true
   */
  grouping?: boolean;
  status?: ToastStatus;
}

export interface ToastConfig {
  placement?: ToastPlacement;
  duration?: number;
  /**
   * Maximum number of live toasts. The oldest toast closes when the limit is
   * exceeded. `undefined` removes the limit.
   * @defaultValue 5
   */
  maxCount?: number;
  hotkey?: string[];
}

export type ToastApiProps = Omit<ToastProps, "type" | "content">;

export interface ToastInstance {
  id: string;
  /**
   * @deprecated Toasts are no longer rendered as standalone VNodes.
   */
  vnode?: VNode;
  close: () => void;
}

export type ToastApi = (
  content: string,
  props?: ToastApiProps
) => ToastInstance;

export type ToastPromiseState<T> =
  | string
  | ToastProps
  | ((value: T) => string | ToastProps);

export interface ToastPromiseOptions<T> {
  loading: string | ToastProps;
  success: ToastPromiseState<T>;
  error: ToastPromiseState<unknown>;
}

export interface ToastEntry {
  (props: ToastProps): ToastInstance;
  info: ToastApi;
  success: ToastApi;
  error: ToastApi;
  warning: ToastApi;
  loading: ToastApi;
  dismiss: (id?: string) => void;
  clear: () => void;
  update: (id: string, props?: ToastProps) => void;
  promise: <T>(
    promise: Promise<T>,
    options: ToastPromiseOptions<T>
  ) => Promise<T>;
  configure: (options: ToastConfig) => void;
}
