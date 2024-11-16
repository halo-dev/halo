<script lang="ts" setup>
import { matchMediaType } from "@/utils/media-type";
import type { Attachment } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useClipboard } from "@vueuse/core";
import { computed, toRefs } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    attachment?: Attachment;
    mountToBody?: boolean;
  }>(),
  {
    attachment: undefined,
    mountToBody: false,
  }
);

const { attachment } = toRefs(props);

const { copy } = useClipboard({ legacy: true });
const { t } = useI18n();

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

const htmlText = computed(() => {
  const { permalink } = attachment.value?.status || {};
  const { displayName } = attachment.value?.spec || {};

  if (isImage.value) {
    return `<img src="${permalink}" alt="${displayName}" />`;
  } else if (isVideo.value) {
    return `<video src="${permalink}"></video>`;
  } else if (isAudio.value) {
    return `<audio src="${permalink}"></audio>`;
  }
  return `<a href="${permalink}">${displayName}</a>`;
});

const markdownText = computed(() => {
  const { permalink } = attachment.value?.status || {};
  const { displayName } = attachment.value?.spec || {};
  if (isImage.value) {
    return `![${displayName}](${permalink})`;
  }
  return `[${displayName}](${permalink})`;
});

const handleCopy = (format: "markdown" | "html" | "url") => {
  const { permalink } = attachment.value?.status || {};

  if (!permalink) return;

  if (format === "url") {
    copy(permalink);
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
        value: attachment?.value?.status?.permalink,
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
  <ul class="flex flex-col space-y-2">
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
