export type Type = "success" | "info" | "warning" | "error";
export const DialogProviderProvideKey = "DIALOG_PROVIDER_PROVIDE_KEY";
import type { Type as ButtonType } from "@/components/button/interface";

export interface useDialogOptions {
  type?: Type;
  visible: boolean;
  title: string;
  description?: string;
  confirmType?: ButtonType;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

export interface DialogProps {
  type?: Type;
  visible?: boolean;
  title?: string;
  description?: string;
  confirmType?: ButtonType;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

export type useDialogUserOptions = Omit<useDialogOptions, "type" | "visible">;
