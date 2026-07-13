<script lang="ts" setup>
import { Toast } from "@halo-dev/components";
import type { Restrictions } from "@uppy/core";
import Uppy from "@uppy/core";
import "@uppy/core/css/style.css";
import "@uppy/dashboard/css/style.css";
import ImageEditor from "@uppy/image-editor";
import "@uppy/image-editor/css/style.min.css";
import en_US from "@uppy/locales/lib/en_US";
import es_ES from "@uppy/locales/lib/es_ES";
import zh_CN from "@uppy/locales/lib/zh_CN";
import zh_TW from "@uppy/locales/lib/zh_TW";
import Dashboard from "@uppy/vue/dashboard";
import XHRUpload from "@uppy/xhr-upload";
import objectHash from "object-hash";
import { h, markRaw, onUnmounted, watch } from "vue";
import { i18n } from "@/locales";
import type { ProblemDetail } from "@/setup/setupApiClient";
import { createHTMLContentModal } from "@/utils/modal";
import type {
  UppyUploadErrorResponse,
  UppyUploadFile,
  UppyUploadRestrictions,
  UppyUploadSuccessResponse,
} from "./types";

const props = withDefaults(
  defineProps<{
    restrictions?: UppyUploadRestrictions;
    meta?: Record<string, unknown>;
    autoProceed?: boolean;
    allowedMetaFields?: string[];
    endpoint: string;
    name?: string;
    note?: string;
    method?: "GET" | "POST" | "PUT" | "HEAD" | "get" | "post" | "put" | "head";
    disabled?: boolean;
    width?: string;
    height?: string;
    doneButtonHandler?: () => void;
  }>(),
  {
    restrictions: undefined,
    meta: undefined,
    autoProceed: false,
    allowedMetaFields: undefined,
    name: "file",
    note: undefined,
    method: "post",
    disabled: false,
    width: "750px",
    height: "550px",
    doneButtonHandler: undefined,
  }
);

const emit = defineEmits<{
  (event: "uploaded", response: UppyUploadSuccessResponse): void;
  (
    event: "error",
    file: UppyUploadFile | undefined,
    response: UppyUploadErrorResponse | undefined
  ): void;
}>();

const locales = {
  en: en_US,
  es: es_ES,
  zh: zh_CN,
  "en-US": en_US,
  "zh-CN": zh_CN,
  "zh-TW": zh_TW,
};

function getUppyLocale(locale: string) {
  return locales[locale] || locales[locale.split("-")[0]] || locales.en;
}

const defaultRestrictions: Restrictions = {
  maxFileSize: null,
  minFileSize: null,
  maxTotalFileSize: null,
  maxNumberOfFiles: null,
  minNumberOfFiles: null,
  allowedFileTypes: null,
  requiredMetaFields: [],
};

const uppy = markRaw(
  new Uppy<Record<string, unknown>, Record<string, unknown>>({
    locale: getUppyLocale(i18n.global.locale.value),
    meta: props.meta,
    restrictions: props.restrictions,
    autoProceed: props.autoProceed,
  })
    .use(XHRUpload, {
      endpoint: `${props.endpoint}`,
      allowedMetaFields: props.allowedMetaFields,
      withCredentials: true,
      formData: true,
      fieldName: props.name,
      method: props.method,
      limit: 5,
      timeout: 0,
      // Uppy 3's XHRUpload failed after the first request. Uppy 5 retries
      // failed requests by default, so keep the existing upload behavior.
      shouldRetry: () => false,
      getResponseData: async (response) => {
        try {
          return JSON.parse(response.responseText);
        } catch (_) {
          // Uppy 3 accepted empty and non-JSON successful responses.
          return {};
        }
      },
      onAfterResponse: async (response) => {
        if (response.status < 200 || response.status >= 300) {
          throw getUploadError(response);
        }
      },
    })
    .use(ImageEditor)
);

function getErrorResponse(
  response: XMLHttpRequest | undefined
): UppyUploadErrorResponse | undefined {
  if (!response || response.status === 0) {
    return undefined;
  }

  let body: unknown = {};

  try {
    body = JSON.parse(response.responseText);
  } catch (_) {
    // Keep the Uppy 3 error event contract, which used an empty object when
    // the response body was not JSON.
  }

  return {
    status: response.status,
    body,
  };
}

function getUploadError(response: XMLHttpRequest) {
  try {
    const body = JSON.parse(response.responseText);
    if (typeof body === "object" && body) {
      const { title, detail } = body as ProblemDetail;
      const message = [title, detail].filter(Boolean).join(": ");

      if (message) {
        return new Error(message);
      }
    }
    return new Error("Internal Server Error");
  } catch (_) {
    return new Error([response.status, response.statusText].join(": "));
  }
}

function showUploadError(response: XMLHttpRequest | undefined) {
  if (!response || response.status === 0) {
    return;
  }

  try {
    const body = JSON.parse(response.responseText);
    if (typeof body === "object" && body) {
      const { title, detail } = body as ProblemDetail;
      const message = [title, detail].filter(Boolean).join(": ");

      if (message) {
        Toast.error(message, { duration: 5000 });
      }
    }
  } catch (_) {
    const { status, statusText } = response;
    const defaultMessage = [status, statusText].join(": ");

    // Catch error requests where the response is text/html,
    // which usually comes from a reverse proxy or WAF.
    const parser = new DOMParser();
    const doc = parser.parseFromString(response.response, "text/html");

    if (Array.from(doc.body.childNodes).some((node) => node.nodeType === 1)) {
      createHTMLContentModal({
        uniqueId: objectHash(response.response || ""),
        title: response.status.toString(),
        width: 700,
        height: "calc(100vh - 20px)",
        centered: true,
        content: h("iframe", {
          srcdoc: response.response,
          sandbox: "",
          referrerpolicy: "no-referrer",
          loading: "lazy",
          style: {
            width: "100%",
            height: "100%",
          },
        }),
      });
      return;
    }

    Toast.error(defaultMessage, { duration: 5000 });
  }
}

const handleUploadSuccess = (
  _: UppyUploadFile | undefined,
  response: UppyUploadSuccessResponse
) => {
  emit("uploaded", response);
};

const handleUploadError = (
  file: UppyUploadFile | undefined,
  _: unknown,
  response: unknown
) => {
  const xhr = response as XMLHttpRequest | undefined;
  showUploadError(xhr);
  emit("error", file, getErrorResponse(xhr));
};

uppy.on("upload-success", handleUploadSuccess);
uppy.on("upload-error", handleUploadError);

watch(
  [
    () => props.meta,
    () => props.restrictions,
    () => props.autoProceed,
    () => props.endpoint,
    () => props.allowedMetaFields,
    () => props.name,
    () => props.method,
  ],
  () => {
    // The previous computed Uppy instance discarded queued files whenever
    // upload-target options changed. Keep that behavior without remounting the
    // Dashboard, otherwise a failed file could be retried to a new target.
    uppy.cancelAll();
    uppy.setOptions({
      meta: props.meta,
      restrictions: {
        ...defaultRestrictions,
        ...props.restrictions,
        requiredMetaFields: props.restrictions?.requiredMetaFields || [],
      },
      autoProceed: props.autoProceed,
    });
    // Uppy merges metadata by default, so replace it explicitly to avoid
    // sending keys removed by the caller.
    uppy.setState({ meta: props.meta || {} });
    uppy.getPlugin("XHRUpload")?.setOptions({
      endpoint: props.endpoint,
      allowedMetaFields: props.allowedMetaFields,
      fieldName: props.name,
      method: props.method,
    });
  },
  { deep: true }
);

watch(
  () => i18n.global.locale.value,
  (locale) => {
    uppy.setOptions({ locale: getUppyLocale(locale) });
  }
);

watch(
  [
    () => props.disabled,
    () => props.note,
    () => props.width,
    () => props.height,
    () => props.doneButtonHandler,
  ],
  () => {
    uppy.getPlugin("Dashboard")?.setOptions({
      disabled: props.disabled,
      note: props.note,
      width: props.width,
      height: props.height,
      doneButtonHandler: props.doneButtonHandler,
    });
  },
  { flush: "post" }
);

onUnmounted(() => {
  uppy.off("upload-success", handleUploadSuccess);
  uppy.off("upload-error", handleUploadError);
  uppy.destroy();
});
</script>

<template>
  <dashboard
    class="w-full"
    :uppy="uppy"
    :props="{
      theme: 'light',
      disabled: disabled,
      note: note,
      width,
      height,
      doneButtonHandler: doneButtonHandler,
    }"
  />
</template>
