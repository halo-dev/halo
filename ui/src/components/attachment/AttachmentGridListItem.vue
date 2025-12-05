<script lang="ts" setup>
import { isImage } from "@/utils/image";
import AttachmentError from "@console/modules/contents/attachments/components/AttachmentError.vue";
import AttachmentLoading from "@console/modules/contents/attachments/components/AttachmentLoading.vue";
import type { Attachment } from "@halo-dev/api-client";
import { IconCheckboxFill } from "@halo-dev/components";
import { UseImage } from "@vueuse/components";
import LazyVideo from "../video/LazyVideo.vue";

withDefaults(
  defineProps<{
    attachment: Attachment;
    isSelected?: boolean;
    isDisabled?: boolean;
  }>(),
  {
    isSelected: false,
    isDisabled: false,
  }
);

const emit = defineEmits<{
  (event: "select"): void;
}>();
</script>
<template>
  <div
    class="group relative inline-flex cursor-pointer flex-col overflow-hidden rounded-base bg-white ring-1 ring-gray-100 transition-all hover:shadow"
    :class="{
      '!ring-primary': isSelected,
      '!ring-red-600': attachment.metadata.deletionTimestamp,
      'pointer-events-none !cursor-not-allowed opacity-50': isDisabled,
    }"
  >
    <div class="aspect-h-8 aspect-w-10 block overflow-hidden bg-gray-100">
      <AttachmentFileTypeIcon
        v-if="!attachment.status?.permalink"
        :file-name="attachment.spec.displayName"
      />
      <UseImage
        v-else-if="isImage(attachment.spec.mediaType)"
        :src="attachment.status?.thumbnails?.S || attachment.status.permalink"
      >
        <template #loading>
          <AttachmentLoading />
        </template>
        <template #error>
          <AttachmentError />
        </template>
        <template #default>
          <Transition appear name="fade">
            <img
              :alt="attachment.spec.displayName"
              :src="
                attachment.status?.thumbnails?.S || attachment.status.permalink
              "
              class="pointer-events-none transform-gpu object-cover group-hover:opacity-75"
            />
          </Transition>
        </template>
      </UseImage>
      <LazyVideo
        v-else-if="attachment?.spec.mediaType?.startsWith('video/')"
        :src="attachment.status?.permalink"
        classes="object-cover group-hover:opacity-75"
      >
        <template #loading>
          <AttachmentLoading />
        </template>
        <template #error>
          <AttachmentError />
        </template>
      </LazyVideo>
      <AttachmentFileTypeIcon v-else :file-name="attachment.spec.displayName" />
    </div>

    <p
      v-tooltip="attachment.spec.displayName"
      class="block cursor-pointer truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
    >
      {{ attachment.spec.displayName }}
    </p>

    <div
      v-if="attachment.metadata.deletionTimestamp"
      class="absolute right-1 top-1 text-xs text-red-300"
    >
      {{ $t("core.common.status.deleting") }}...
    </div>

    <div
      v-if="!attachment.metadata.deletionTimestamp"
      v-permission="['system:attachments:manage']"
      :class="{
        '!flex': isSelected,
      }"
      class="absolute left-0 top-0 hidden h-1/3 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
    >
      <slot name="actions" />
      <IconCheckboxFill
        :class="{
          '!text-primary': isSelected,
        }"
        class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
        @click.stop="emit('select')"
      />
    </div>
  </div>
</template>
