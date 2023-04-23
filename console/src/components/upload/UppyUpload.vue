<script lang="ts" setup>
import { Dashboard } from "@uppy/vue";
import "@uppy/core/dist/style.css";
import "@uppy/dashboard/dist/style.css";
import Uppy, { type SuccessResponse } from "@uppy/core";
import type { Restrictions } from "@uppy/core";
import XHRUpload from "@uppy/xhr-upload";
import zh_CN from "@uppy/locales/lib/zh_CN";
import zh_TW from "@uppy/locales/lib/zh_TW";
import en_US from "@uppy/locales/lib/en_US";
import { computed, onUnmounted } from "vue";
import { Toast } from "@halo-dev/components";
import type { ProblemDetail } from "@/utils/api-client";
import { i18n } from "@/locales";

const props = withDefaults(
  defineProps<{
    restrictions?: Restrictions;
    meta?: Record<string, unknown>;
    autoProceed?: boolean;
    allowedMetaFields?: string[];
    endpoint: string;
    name?: string;
    note?: string;
    method?: "GET" | "POST" | "PUT" | "HEAD" | "get" | "post" | "put" | "head";
    disabled?: boolean;
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
    doneButtonHandler: undefined,
  }
);

const emit = defineEmits<{
  (event: "uploaded", response: SuccessResponse): void;
  (event: "error", file, response): void;
}>();

const locales = {
  en: en_US,
  zh: zh_CN,
  "en-US": en_US,
  "zh-CN": zh_CN,
  "zh-TW": zh_TW,
};

const uppy = computed(() => {
  return new Uppy({
    locale: locales[i18n.global.locale.value] || locales["zh-CN"],
    meta: props.meta,
    restrictions: props.restrictions,
    autoProceed: props.autoProceed,
  }).use(XHRUpload, {
    endpoint: `${import.meta.env.VITE_API_URL}${props.endpoint}`,
    allowedMetaFields: props.allowedMetaFields,
    withCredentials: true,
    formData: true,
    fieldName: props.name,
    method: props.method,
    limit: 5,
    timeout: 0,
    getResponseError: (responseText) => {
      const response = JSON.parse(responseText);
      const { title, detail } = (response || {}) as ProblemDetail;
      const message = [title, detail].filter(Boolean).join(": ");

      if (message) {
        Toast.error(message, { duration: 5000 });

        return new Error(message);
      }
      return new Error("Internal Server Error");
    },
  });
});

uppy.value.on("upload-success", (_, response: SuccessResponse) => {
  emit("uploaded", response);
});

uppy.value.on("upload-error", (file, _, response) => {
  emit("error", file, response);
});

onUnmounted(() => {
  uppy.value.close({ reason: "unmount" });
});
</script>

<template>
  <dashboard
    :uppy="uppy"
    :props="{
      theme: 'light',
      disabled: disabled,
      note: note,
      doneButtonHandler: doneButtonHandler,
    }"
  />
</template>
