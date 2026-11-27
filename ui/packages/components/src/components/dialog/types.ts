import type { Component } from "vue";
import type { ButtonType } from "../button/types";

export type DialogType = "success" | "info" | "warning" | "error";

export type DialogSize = "sm" | "md" | "lg";

export interface DialogProps {
  type?: DialogType;
  size?: DialogSize;
  visible?: boolean;
  title?: string;
  description?: string;
  confirmType?: ButtonType;
  showCancel?: boolean;
  showClose?: boolean;
  closeOnEsc?: boolean;
  layerClosable?: boolean;
  confirmText?: string;
  cancelText?: string;
  uniqueId?: string;
  /**
   * Overrides the default icon. Ignored when the `icon` slot is provided.
   */
  icon?: Component;
  /**
   * Custom width in pixels. Takes precedence over `size`.
   */
  width?: number;
  /**
   * Runs when the dialog is dismissed via the cancel button, close button,
   * overlay or Escape. Not called when `close()` is invoked programmatically.
   */
  onCancel?: () => void;
  /**
   * Runs when the confirm button is clicked. When it returns a promise, the
   * confirm button shows a loading state. A rejection keeps the dialog open so
   * the user can retry.
   */
  onConfirm?: () => void | Promise<void>;
}
