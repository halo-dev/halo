<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconFolder } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/console-shared";
import { defineAsyncComponent, ref, type PropType } from "vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const attachmentSelectorModalVisible = ref(false);

const AttachmentSelectorModal = defineAsyncComponent({
  loader: () => {
    if (utils.permission.has(["system:attachments:view"])) {
      return import(
        "@console/modules/contents/attachments/components/AttachmentSelectorModal.vue"
      );
    }
    return import(
      "@uc/modules/contents/attachments/components/AttachmentSelectorModal.vue"
    );
  },
});

const onInput = (e: Event) => {
  props.context.handlers.DOMInput(e);
};

const onAttachmentSelect = (attachments: AttachmentLike[]) => {
  if (!attachments.length) {
    return;
  }
  const attachment = attachments[0];
  props.context.node.input(utils.attachment.getUrl(attachment));
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

  <HasPermission
    :permissions="['uc:attachments:manage', 'system:attachments:view']"
  >
    <div
      class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
      @click="attachmentSelectorModalVisible = true"
    >
      <IconFolder class="h-4 w-4 text-gray-500 group-hover:text-gray-700" />
    </div>
  </HasPermission>

  <AttachmentSelectorModal
    v-if="attachmentSelectorModalVisible"
    :accepts="context.accepts as string[]"
    :min="1"
    :max="1"
    @select="onAttachmentSelect"
    @close="attachmentSelectorModalVisible = false"
  />
</template>
