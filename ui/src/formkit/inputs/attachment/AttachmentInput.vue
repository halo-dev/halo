<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  IconAddCircle,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
} from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { computed, useTemplateRef, type PropType } from "vue";
import { useDraggable } from "vue-draggable-plus";
import MingcuteMore2Line from "~icons/mingcute/more-2-line";
import AttachmentDropdownItem from "./AttachmentDropdownItem.vue";
import AttachmentPreview from "./AttachmentPreview.vue";
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

const currentValue = computed({
  get() {
    if (!props.context._value) {
      return [];
    }
    if (multiple.value) {
      return props.context._value as string[];
    }
    return [props.context._value as string];
  },
  set(value: string[]) {
    props.context.node.input(value);
  },
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

const handleRemove = (index: number) => {
  if (multiple.value) {
    props.context.node.input(currentValue.value.filter((_, i) => i !== index));
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

// Drag
const container = useTemplateRef<HTMLDivElement>("container");

useDraggable(container, currentValue, {
  disabled: !multiple.value,
  draggable: "[data-draggable='true']",
});
</script>
<template>
  <div ref="container" class="inline-flex w-full flex-wrap gap-2">
    <div
      v-for="(item, index) in currentValue"
      :key="item"
      data-draggable="true"
      class="group/attachment-item relative overflow-hidden rounded-lg border bg-white"
      :style="{ width: width, aspectRatio: aspectRatio }"
    >
      <AttachmentPreview :url="item" />

      <!-- @vue-ignore -->
      <VDropdown
        class="absolute right-1 top-1 inline-flex"
        :dispose-timeout="null"
      >
        <template #default="{ shown }">
          <button
            type="button"
            class="inline-flex size-5 items-center justify-center rounded transition-all"
            :class="{
              'bg-primary opacity-100': shown,
              'bg-primary/80 opacity-0 hover:bg-primary active:bg-primary/80 group-hover/attachment-item:opacity-100':
                !shown,
            }"
          >
            <MingcuteMore2Line class="size-4 text-white" />
          </button>
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
          <VDropdownItem type="danger" @click="handleRemove(index)">
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
        class="group inline-flex size-full items-center justify-center rounded-lg border border-dashed transition-colors hover:border-primary"
      >
        <IconAddCircle
          class="text-gray-600 transition-colors group-hover:text-primary"
        />
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
