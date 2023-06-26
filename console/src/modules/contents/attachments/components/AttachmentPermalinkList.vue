<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import type { Attachment } from "@halo-dev/api-client";
import { useAttachmentPermalinkCopy } from "../composables/use-attachment";
import { toRefs, computed } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    attachment?: Attachment;
    mountToBody?: boolean;
  }>(),
  {
    visible: false,
    attachment: undefined,
    mountToBody: false,
  }
);

const { attachment } = toRefs(props);

const { handleCopy, htmlText, markdownText } =
  useAttachmentPermalinkCopy(attachment);

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
