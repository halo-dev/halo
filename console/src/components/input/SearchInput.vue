<script lang="ts" setup>
import { reset } from "@formkit/core";
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

const id = `search-form-${crypto.randomUUID()}`;

function onKeywordChange(data: { keyword: string }) {
  emit("update:modelValue", data.keyword);
}

function handleReset() {
  emit("update:modelValue", "");
  reset(id);
}
</script>

<template>
  <FormKit :id="id" type="form" @submit="onKeywordChange">
    <FormKit
      outer-class="!p-0"
      :placeholder="placeholder || $t('core.common.placeholder.search')"
      type="text"
      name="keyword"
      :model-value="modelValue"
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
  </FormKit>
</template>
