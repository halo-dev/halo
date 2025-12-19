<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { GetThumbnailByUriSizeEnum } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VButton,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
} from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { computed, type PropType } from "vue";
import AttachmentDropdownItem from "./AttachmentDropdownItem.vue";
import CustomLinkDropdownItem from "./CustomLinkDropdownItem.vue";
import UploadDropdownItem from "./UploadDropdownItem.vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const multiple = computed(() => props.context.multiple as boolean);
const width = computed(() => props.context.width as string);
const aspectRatio = computed(() => props.context.aspectRatio as string);
const accepts = computed(() => {
  return props.context.accepts as string[];
});

const currentValue = computed(() => {
  if (!props.context._value) {
    return [];
  }
  if (multiple.value) {
    return props.context._value as string[];
  }
  return [props.context._value as string];
});

function onAttachmentsSelect(attachments: AttachmentLike[]) {
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

function onAttachmentReplace(index: number, attachments: AttachmentLike[]) {
  if (!attachments.length) {
    return;
  }
  if (multiple.value) {
    const newAttachments = [...currentValue.value];

    const url = utils.attachment.getUrl(attachments[0]);
    if (!url) {
      return;
    }

    newAttachments[index] = url;
    props.context.node.input(newAttachments);
  } else {
    props.context.node.input(utils.attachment.getUrl(attachments[0]));
  }
}

const handleRemove = (item: string) => {
  if (multiple.value) {
    props.context.node.input(
      currentValue.value.filter((value) => value !== item)
    );
  } else {
    props.context.node.input(undefined);
  }
};

function onCustomLinkSubmit(url: string) {
  if (multiple.value) {
    props.context.node.input([...currentValue.value, url]);
  } else {
    props.context.node.input(url);
  }
}

function onLinkReplace(index: number, url: string) {
  if (multiple.value) {
    const newAttachments = [...currentValue.value];
    newAttachments[index] = url;
    props.context.node.input(newAttachments);
  } else {
    props.context.node.input(url);
  }
}
</script>
<template>
  <div class="inline-flex w-full flex-wrap gap-2">
    <div
      v-for="(item, index) in currentValue"
      :key="item"
      class="group relative overflow-hidden rounded-lg border"
      :style="{ width: width, aspectRatio: aspectRatio }"
    >
      <img
        :src="
          utils.attachment.getThumbnailUrl(item, GetThumbnailByUriSizeEnum.S)
        "
        class="size-full object-cover"
      />

      <!-- @vue-ignore -->
      <VDropdown
        class="absolute right-1 top-1 inline-flex"
        :dispose-timeout="null"
      >
        <template #default="{ shown }">
          <VButton
            class="group-hover:opacity-100"
            :class="{ 'opacity-100': shown, 'opacity-0': !shown }"
            size="xs"
            type="secondary"
          >
            {{ $t("core.common.buttons.replace") }}
          </VButton>
        </template>
        <template #popper>
          <UploadDropdownItem
            :multiple="false"
            :accepts="accepts"
            @selected="(attachments) => onAttachmentReplace(index, attachments)"
          />
          <AttachmentDropdownItem
            :multiple="false"
            :accepts="accepts"
            @selected="(attachments) => onAttachmentReplace(index, attachments)"
          />
          <CustomLinkDropdownItem
            :url="item"
            @submit="(url) => onLinkReplace(index, url)"
          />
          <VDropdownDivider />
          <VDropdownItem type="danger" @click="handleRemove(item)">
            {{ $t("core.common.buttons.remove") }}
          </VDropdownItem>
        </template>
      </VDropdown>
    </div>

    <!-- @vue-ignore -->
    <VDropdown
      v-if="multiple || currentValue.length === 0"
      class="inline-flex"
      :style="{ width: width, aspectRatio: aspectRatio }"
      :dispose-timeout="null"
    >
      <button
        type="button"
        class="group inline-flex size-full items-center justify-center rounded-lg border border-dashed"
      >
        <IconAddCircle class="text-gray group-hover:text-gray-900" />
      </button>
      <template #popper>
        <UploadDropdownItem
          :multiple="multiple"
          :accepts="accepts"
          @selected="onAttachmentsSelect"
        />
        <AttachmentDropdownItem
          :multiple="multiple"
          :accepts="accepts"
          @selected="onAttachmentsSelect"
        />
        <CustomLinkDropdownItem @submit="onCustomLinkSubmit" />
      </template>
    </VDropdown>
  </div>
</template>
