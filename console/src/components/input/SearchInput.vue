<script lang="ts" setup>
import { randomUUID } from "@/utils/id";
import { getNode, reset } from "@formkit/core";
import { IconCloseCircle } from "@halo-dev/components";

withDefaults(
  defineProps<{
    placeholder?: string;
    modelValue: string;
  }>(),
  {
    placeholder: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", modelValue: string): void;
}>();

const id = `search-input-${randomUUID()}`;

function handleReset() {
  emit("update:modelValue", "");
  reset(id);
}

function onKeywordChange() {
  const keywordNode = getNode(id);
  if (keywordNode) {
    emit("update:modelValue", keywordNode._value as string);
  }
}
</script>

<template>
  <FormKit
    :id="id"
    outer-class="!p-0"
    :placeholder="placeholder || $t('core.common.placeholder.search')"
    type="text"
    name="keyword"
    :model-value="modelValue"
    @keyup.enter="onKeywordChange"
  >
    <template v-if="modelValue" #suffix>
      <div
        class="group flex h-full cursor-pointer items-center bg-white px-2 transition-all hover:bg-gray-50"
        @click="handleReset"
      >
        <IconCloseCircle
          class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
        />
      </div>
    </template>
  </FormKit>
</template>
