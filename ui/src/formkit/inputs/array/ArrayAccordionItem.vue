<script setup lang="ts">
import type { FormKitNode } from "@formkit/core";
import { VAccordionItem, VButton, VSpace } from "@halo-dev/components";
import { cloneDeepWith } from "es-toolkit/compat";
import { computed, ref, watch } from "vue";
import MingcuteDotsLine from "~icons/mingcute/dots-line";
import ArrayItemForm from "./ArrayItemForm.vue";
import ArrayItemLabels from "./ArrayItemLabels.vue";
import type { FormattedItemLabel } from "./types";

const props = defineProps<{
  node: FormKitNode;
  value: string;
  item: Record<string, unknown>;
  labels?: FormattedItemLabel[];
  open: boolean;
  dragHandleClass: string;
  isDraft?: boolean;
  removeControl?: boolean;
  canRemove?: boolean;
}>();

const emit = defineEmits<{
  (event: "save", data: Record<string, unknown>): void;
  (event: "cancel"): void;
  (event: "remove"): void;
}>();

const formRef = ref<InstanceType<typeof ArrayItemForm> | null>(null);
const draft = ref<Record<string, unknown>>({});

watch(
  () => props.open,
  (open) => {
    if (open) {
      draft.value = cloneDeepWith(props.item) ?? {};
    }
  },
  { immediate: true }
);

const classNames = computed(() => ({
  item: [
    "array-accordion-item",
    ...(props.isDraft ? ["array-accordion-item--draft"] : []),
  ],
}));

const handleSubmit = (data: Record<string, unknown>) => {
  emit("save", data);
};

const commit = async () => {
  return (await formRef.value?.submit()) ?? false;
};

defineExpose({
  commit,
  value: props.value,
});
</script>

<template>
  <VAccordionItem
    :value="value"
    :class-names="classNames"
    header-as="div"
    :unmount-on-hide="true"
  >
    <template #trigger>
      <span class="flex min-w-0 flex-1 items-center gap-2.5 font-normal">
        <MingcuteDotsLine
          v-if="!isDraft"
          class="drag-handle size-4 flex-none cursor-move text-gray-500"
          :class="dragHandleClass"
          @click.stop
        />
        <span
          class="line-clamp-1 flex min-w-0 flex-1 items-center gap-2 whitespace-nowrap text-sm text-gray-900"
        >
          <ArrayItemLabels
            v-if="!isDraft"
            :labels="labels"
            :link-images="false"
          />
          <span v-else class="text-gray-500">
            {{ $t("core.formkit.array.new_item") }}
          </span>
        </span>
      </span>
    </template>

    <div class="px-4 py-4">
      <ArrayItemForm
        ref="formRef"
        :node="node"
        :item-value="draft"
        @submit="handleSubmit"
      />
      <div class="mt-4 flex flex-wrap items-center justify-between gap-3">
        <VSpace>
          <VButton type="secondary" size="sm" @click="formRef?.submit()">
            {{ $t("core.common.buttons.save") }}
          </VButton>
          <VButton size="sm" @click="emit('cancel')">
            {{ $t("core.common.buttons.cancel") }}
          </VButton>
        </VSpace>
        <VButton
          v-if="removeControl"
          type="danger"
          variant="ghost"
          size="sm"
          :disabled="!canRemove"
          @click="emit('remove')"
        >
          {{ $t("core.common.buttons.delete") }}
        </VButton>
      </div>
    </div>
  </VAccordionItem>
</template>
