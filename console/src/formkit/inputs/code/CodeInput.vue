<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import type { PropType } from "vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const height = props.context.height as string;
const language = props.context.language as string;

const onChange = (value: string) => {
  props.context.node.input(value);
};
</script>

<template>
  <Suspense>
    <VCodemirror
      :model-value="props.context._value"
      v-bind="context.attrs"
      :height="height"
      :language="language"
      class="block w-full"
      @change="onChange"
    />
    <template #fallback>
      <span class="p-1 text-xs text-gray-400">
        {{ $t("core.common.status.loading") }}...
      </span>
    </template>
  </Suspense>
</template>
