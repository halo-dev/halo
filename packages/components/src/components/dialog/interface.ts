export type Type = "success" | "info" | "warning" | "error";
export const DialogProviderProvideKey = "DIALOG_PROVIDER_PROVIDE_KEY";

export interface useDialogOptions {
  type?: Type;
  visible: boolean;
  title: string;
  description?: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

export type useDialogUserOptions = Omit<useDialogOptions, "type" | "visible">;
