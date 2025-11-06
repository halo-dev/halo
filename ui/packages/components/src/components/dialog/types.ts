export type DialogType = "success" | "info" | "warning" | "error";
export const DialogProviderProvideKey = "DIALOG_PROVIDER_PROVIDE_KEY";
import type { ButtonType } from "../button/types";

export interface DialogProps {
  type?: DialogType;
  visible?: boolean;
  title?: string;
  description?: string;
  confirmType?: ButtonType;
  showCancel?: boolean;
  confirmText?: string;
  cancelText?: string;
  uniqueId?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}
