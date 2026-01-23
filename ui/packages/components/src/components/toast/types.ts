export type ToastType = "success" | "info" | "warning" | "error";

export interface ToastProps {
  type?: ToastType;
  content?: string;
  duration?: number;
  closable?: boolean;
  frozenOnHover?: boolean;
  count?: 0;
  onClose?: () => void;
}
