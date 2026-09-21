import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { nextTick, ref } from "vue";

interface SudoProblemDetail {
  type?: string;
}

export const SUDO_REQUIRED_TYPE = "https://halo.run/probs/sudo-required";

const visible = ref(false);

let inFlight: Promise<void> | null = null;
let resolveConfirm: (() => void) | null = null;
let rejectConfirm: ((reason?: unknown) => void) | null = null;

export function useSudoConfirmModalState() {
  return { visible };
}

export function isSudoRequiredError(error: AxiosError): boolean {
  const data = error.response?.data as SudoProblemDetail | undefined;
  return error.response?.status === 403 && data?.type === SUDO_REQUIRED_TYPE;
}

export function requestSudoConfirm(): Promise<void> {
  if (inFlight) {
    return inFlight;
  }
  inFlight = new Promise<void>((resolve, reject) => {
    resolveConfirm = resolve;
    rejectConfirm = reject;
  }).finally(() => {
    inFlight = null;
  });
  // Open after the failing mutation has finished patching, so VModal is not
  // inserted into a node OverlayScrollbars already rewrote.
  nextTick(() => {
    visible.value = true;
  });
  return inFlight;
}

export function completeSudoConfirm() {
  visible.value = false;
  resolveConfirm?.();
}

export function cancelSudoConfirm() {
  visible.value = false;
  rejectConfirm?.(new Error("sudo cancelled"));
}

export function markSudoRetried(
  config: InternalAxiosRequestConfig
): InternalAxiosRequestConfig {
  return {
    ...config,
    headers: config.headers,
    _sudoRetried: true,
  } as InternalAxiosRequestConfig;
}

export function wasSudoRetried(config?: InternalAxiosRequestConfig): boolean {
  return Boolean(
    (config as InternalAxiosRequestConfig & { _sudoRetried?: boolean })
      ?._sudoRetried
  );
}
