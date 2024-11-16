import { i18n } from "@/locales";
import { axiosInstance } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { AxiosError } from "axios";

export interface ProblemDetail {
  detail: string;
  instance: string;
  status: number;
  title: string;
  type?: string;
}

export function setupApiClient() {
  axiosInstance.interceptors.response.use(
    (response) => {
      return response;
    },
    async (error: AxiosError<ProblemDetail>) => {
      if (error.code === "ERR_CANCELED") {
        return Promise.reject(error);
      }

      if (/Network Error/.test(error.message)) {
        // @ts-ignore
        Toast.error(i18n.global.t("core.common.toast.network_error"));
        return Promise.reject(error);
      }

      const errorResponse = error.response;

      if (!errorResponse) {
        Toast.error(i18n.global.t("core.common.toast.network_error"));
        return Promise.reject(error);
      }

      // Don't show error toast
      // see https://github.com/halo-dev/halo/issues/2836
      if (errorResponse.config.mute) {
        return Promise.reject(error);
      }

      const { status } = errorResponse;
      const { title, detail } = errorResponse.data;

      if (status === 401) {
        Dialog.warning({
          title: i18n.global.t("core.common.dialog.titles.login_expired"),
          description: i18n.global.t(
            "core.common.dialog.descriptions.login_expired"
          ),
          confirmType: "secondary",
          confirmText: i18n.global.t("core.common.buttons.confirm"),
          cancelText: i18n.global.t("core.common.buttons.cancel"),
          uniqueId: "login_expired",
          onConfirm: () => {
            const currentPath = `${location.pathname}${location.search}`;
            location.href = `/login?redirect_uri=${encodeURIComponent(
              currentPath
            )}`;
          },
        });

        return Promise.reject(error);
      }

      if (title || detail) {
        Toast.error(detail || title);
        return Promise.reject(error);
      }

      // Final fallback
      if (errorResponse.status) {
        const { status, statusText } = errorResponse;
        Toast.error([status, statusText].filter(Boolean).join(": "));
        return Promise.reject(error);
      }

      Toast.error(i18n.global.t("core.common.toast.unknown_error"));

      return Promise.reject(error);
    }
  );
}
