<script lang="ts" setup>
import { Icon } from "@iconify/vue";
import { computed } from "vue";
import type { ArrayItemLabelType } from "..";
import type { IconifyFormat, IconifyValue } from "../../iconify/types";

const props = defineProps<{
  itemLabel: {
    type: ArrayItemLabelType;
    value: string | IconifyValue;
    format: IconifyFormat;
    valueOnly?: boolean;
  };
}>();

const value = computed(() => {
  if (props.itemLabel.valueOnly) {
    return props.itemLabel.value as string;
  }
  return (props.itemLabel.value as IconifyValue)?.value;
});
</script>
<template>
  <div class="inline-flex items-center [&>*]:h-4">
    <img
      v-if="['url', 'dataurl'].includes(itemLabel.format)"
      :src="value"
      class="max-w-none"
    />
    <Icon v-else-if="itemLabel.format === 'name'" :icon="value" />
    <div
      v-else
      class="inline-flex items-center justify-center"
      v-html="value"
    ></div>
  </div>
</template>
