<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  consoleApiClient,
  GetThumbnailByUriSizeEnum,
} from "@halo-dev/api-client";
import {
  IconAddCircle,
  VButton,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { useFileDialog } from "@vueuse/core";
import { computed, ref, type PropType } from "vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const multiple = computed(() => props.context.multiple as boolean);
const max = computed(() => props.context.max as number);
const width = computed(() => props.context.width as string);
const aspectRatio = computed(() => props.context.aspectRatio as string);
const currentValue = computed(() => {
  if (!props.context._value) {
    return [];
  }
  if (multiple.value) {
    return props.context._value as string[];
  }
  return [props.context._value as string];
});

const selectorModalVisible = ref(false);

const currentRestrictionCount = computed(() => {
  if (!multiple.value) {
    return 1 - currentValue.value.length;
  }
  if (!max.value && multiple.value) {
    return Infinity;
  }
  return max.value - currentValue.value.length;
});

function onImageSelect(attachments: AttachmentLike[]) {
  if (!attachments.length) {
    return;
  }
  if (multiple.value) {
    props.context.node.input([
      ...currentValue.value,
      ...attachments.map((attachment) => utils.attachment.getUrl(attachment)),
    ]);
  } else {
    props.context.node.input(utils.attachment.getUrl(attachments[0]));
  }
}

// Upload
const { onChange: onImageInputChange, open: openImageInputDialog } =
  useFileDialog({
    accept: "image/*",
    multiple: multiple.value,
  });

onImageInputChange((files) => {
  if (!files?.length) {
    return;
  }

  for (const file of files) {
    consoleApiClient.storage.attachment
      .uploadAttachment({
        policyName: "default-policy",
        file: file,
      })
      .then((response) => {
        const url = utils.attachment.getUrl(response.data);
        if (url) {
          if (multiple.value) {
            props.context.node.input([...currentValue.value, url]);
          } else {
            props.context.node.input(url);
          }
        }
      });
  }
});

const handleRemove = (image: string) => {
  if (multiple.value) {
    props.context.node.input(
      currentValue.value.filter((value) => value !== image)
    );
  } else {
    props.context.node.input(undefined);
  }
};
</script>
<template>
  <div class="flex flex-wrap gap-2">
    <div
      v-for="image in currentValue"
      :key="image"
      class="group relative overflow-hidden rounded-lg border"
      :style="{ width: width, aspectRatio: aspectRatio }"
    >
      <img
        :src="
          utils.attachment.getThumbnailUrl(image, GetThumbnailByUriSizeEnum.S)
        "
        class="size-full object-cover"
      />
      <VButton
        class="absolute right-1 top-1 opacity-0 group-hover:opacity-100"
        size="xs"
        type="secondary"
        @click="handleRemove(image)"
      >
        移除
      </VButton>
    </div>

    <VDropdown
      v-if="multiple || currentValue.length === 0"
      :style="{ width: width, aspectRatio: aspectRatio }"
    >
      <button
        type="button"
        class="group inline-flex size-full items-center justify-center rounded-lg border border-dashed"
      >
        <IconAddCircle class="text-gray group-hover:text-gray-900" />
      </button>
      <template #popper>
        <VDropdownItem @click="openImageInputDialog()"> 上传 </VDropdownItem>
        <VDropdownItem @click="selectorModalVisible = true">
          从附件库选择
        </VDropdownItem>
      </template>
    </VDropdown>
  </div>
  <AttachmentSelectorModal
    v-if="selectorModalVisible"
    :max="currentRestrictionCount"
    :accepts="['image/*']"
    @select="onImageSelect"
    @close="selectorModalVisible = false"
  />
</template>
