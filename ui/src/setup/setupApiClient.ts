import { i18n } from "@/locales";
import { createHTMLContentModal } from "@/utils/modal";
import { axiosInstance } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { AxiosError } from "axios";
import objectHash from "object-hash";
import { h } from "vue";

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
    async (error: AxiosError) => {
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
      const { title, detail } = errorResponse.data as ProblemDetail;

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

      // Catch error requests where the response is text/html,
      // which usually comes from a reverse proxy or WAF

      const contentType = error.response?.headers["content-type"];

      if (contentType.toLowerCase().includes("text/html")) {
        createHTMLContentModal({
          uniqueId: objectHash(error.response?.data || ""),
          title: error.response?.status.toString(),
          width: 700,
          height: "calc(100vh - 20px)",
          centered: true,
          content: h("iframe", {
            srcdoc: error.response?.data?.toString(),
            sandbox: "",
            referrerpolicy: "no-referrer",
            loading: "lazy",
            style: {
              width: "100%",
              height: "100%",
            },
          }),
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
