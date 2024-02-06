export type Type = "success" | "info" | "warning" | "error";

export interface ToastProps {
  type?: Type;
  content?: string;
  duration?: number;
  closable?: boolean;
  frozenOnHover?: boolean;
  count?: 0;
  onClose?: () => void;
}
