<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconFolder } from "@halo-dev/components";
import AttachmentSelectorModal from "@/modules/contents/attachments/components/AttachmentSelectorModal.vue";
import { ref, type PropType } from "vue";
import type { AttachmentLike } from "@halo-dev/console-shared";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const attachmentSelectorModal = ref(false);

const onInput = (e: Event) => {
  props.context.handlers.DOMInput(e);
};

const onAttachmentSelect = (attachments: AttachmentLike[]) => {
  const urls: (string | undefined)[] = attachments.map((attachment) => {
    if (typeof attachment === "string") {
      return attachment;
    }
    if ("url" in attachment) {
      return attachment.url;
    }
    if ("spec" in attachment) {
      return attachment.status?.permalink;
    }
  });

  if (urls.length) {
    props.context.node.input(urls[0]);
  }
};
</script>

<template>
  <input
    :id="context.id"
    :value="context._value"
    :class="context.classes.input"
    :name="context.node.name"
    v-bind="context.attrs"
    type="text"
    @blur="context.handlers.blur()"
    @input="onInput"
  />
  <div
    class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
    @click="attachmentSelectorModal = true"
  >
    <IconFolder class="h-4 w-4 text-gray-500 group-hover:text-gray-700" />
  </div>
  <AttachmentSelectorModal
    v-model:visible="attachmentSelectorModal"
    :accepts="context.accepts as string[]"
    :min="1"
    :max="1"
    @select="onAttachmentSelect"
  />
</template>
