export type Type = "success" | "info" | "warning" | "error";
export const DialogProviderProvideKey = "DIALOG_PROVIDER_PROVIDE_KEY";
import type { Type as ButtonType } from "../button/interface";

export interface DialogProps {
  type?: Type;
  visible?: boolean;
  title?: string;
  description?: string;
  confirmType?: ButtonType;
  showCancel?: boolean;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

type useDialogOptions = Omit<DialogProps, "visible" | "title"> & {
  visible: boolean;
  title: string;
};

export type useDialogUserOptions = Omit<useDialogOptions, "type" | "visible">;
