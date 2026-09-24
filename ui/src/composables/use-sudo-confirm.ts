import { axiosInstance } from "@halo-dev/api-client";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { nextTick, ref } from "vue";

interface SudoProblemDetail {
  type?: string;
}

export const SUDO_REQUIRED_TYPE = "https://halo.run/probs/sudo-required";

/** Mirrors {@code SudoService.SUDO_TTL} on the backend. */
export const SUDO_TTL_MINUTES = 30;

/** Mirrors the {@code send-sudo-code} rate limiter on the backend. */
export const SUDO_RESEND_INTERVAL_SECONDS = 60;

export interface SudoMethod {
  name: string;
  canSendCode: boolean;
  maskedTarget?: string;
}

export interface SudoStatus {
  active: boolean;
  expiresAt?: string | null;
  methods: SudoMethod[];
}

export function fetchSudoStatus() {
  return axiosInstance.get<SudoStatus>("/sudo");
}

export function sendSudoCode(method: string) {
  return axiosInstance.post("/sudo/code", new URLSearchParams({ method }));
}

export function confirmSudo(method: string, code: string) {
  return axiosInstance.post(
    "/sudo/confirm",
    new URLSearchParams({ method, code })
  );
}

const sudoConfirmVisible = ref(false);

let inFlight: Promise<void> | null = null;
let resolveConfirm: (() => void) | null = null;
let rejectConfirm: ((reason?: unknown) => void) | null = null;

export { sudoConfirmVisible };

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
    sudoConfirmVisible.value = true;
  });
  return inFlight;
}

export function completeSudoConfirm() {
  sudoConfirmVisible.value = false;
  resolveConfirm?.();
}

export function cancelSudoConfirm() {
  sudoConfirmVisible.value = false;
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
