<script lang="ts" setup>
import { useGlobalInfoStore } from "@/stores/global-info";
import { matchMediaType } from "@/utils/media-type";
import type { Attachment } from "@halo-dev/api-client";
import { Toast, VButton, VTabbar } from "@halo-dev/components";
import { useClipboard } from "@vueuse/core";
import { computed, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    attachment?: Attachment;
  }>(),
  {
    attachment: undefined,
  }
);

const { attachment } = toRefs(props);

const { copy } = useClipboard({ legacy: true });
const { t } = useI18n();
const { globalInfo } = useGlobalInfoStore();

const activeId = ref<"relative" | "absolute">("relative");

const mediaType = computed(() => {
  return attachment.value?.spec.mediaType;
});

const isImage = computed(() => {
  return mediaType.value && matchMediaType(mediaType.value, "image/*");
});

const isVideo = computed(() => {
  return mediaType.value && matchMediaType(mediaType.value, "video/*");
});

const isAudio = computed(() => {
  return mediaType.value && matchMediaType(mediaType.value, "audio/*");
});

const isLocalAttachment = computed(() => {
  return props.attachment?.status?.permalink?.startsWith("/");
});

const permalink = computed(() => {
  const { permalink: value } = props.attachment?.status || {};

  if (!isLocalAttachment.value) {
    return value;
  }

  if (activeId.value === "relative") {
    return value;
  }

  return `${globalInfo?.externalUrl}${value}`;
});

const htmlText = computed(() => {
  const { displayName } = attachment.value?.spec || {};

  if (isImage.value) {
    return `<img src="${permalink.value}" alt="${displayName}" />`;
  } else if (isVideo.value) {
    return `<video src="${permalink.value}"></video>`;
  } else if (isAudio.value) {
    return `<audio src="${permalink.value}"></audio>`;
  }
  return `<a href="${permalink.value}">${displayName}</a>`;
});

const markdownText = computed(() => {
  const { displayName } = attachment.value?.spec || {};
  if (isImage.value) {
    return `![${displayName}](${permalink.value})`;
  }
  return `[${displayName}](${permalink.value})`;
});

const handleCopy = (format: "markdown" | "html" | "url") => {
  if (format === "url") {
    copy(permalink.value || "");
  } else if (format === "markdown") {
    copy(markdownText.value);
  } else if (format === "html") {
    copy(htmlText.value);
  }

  Toast.success(t("core.common.toast.copy_success"));
};

const formats = computed(
  (): {
    label: string;
    key: "url" | "html" | "markdown";
    value?: string;
  }[] => {
    return [
      {
        label: "URL",
        key: "url",
        value: permalink.value,
      },
      {
        label: "HTML",
        key: "html",
        value: htmlText.value,
      },
      {
        label: "Markdown",
        key: "markdown",
        value: markdownText.value,
      },
    ];
  }
);
</script>

<template>
  <VTabbar
    v-if="isLocalAttachment"
    v-model:active-id="activeId"
    :items="[
      {
        label: $t('core.attachment.permalink_list.relative'),
        id: 'relative',
      },
      {
        label: $t('core.attachment.permalink_list.absolute'),
        id: 'absolute',
      },
    ]"
    type="outline"
  ></VTabbar>
  <ul class="mt-3 flex flex-col space-y-2">
    <li v-for="format in formats" :key="format.key">
      <div
        class="flex w-full cursor-pointer items-center justify-between space-x-3 rounded border p-3 hover:border-primary"
      >
        <div class="flex flex-1 flex-col space-y-2 text-xs text-gray-900">
          <span class="font-semibold">
            {{ format.label }}
          </span>
          <span class="break-all">
            {{ format.value }}
          </span>
        </div>
        <div>
          <VButton size="sm" @click="handleCopy(format.key)">
            {{ $t("core.common.buttons.copy") }}
          </VButton>
        </div>
      </div>
    </li>
  </ul>
</template>
