<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { VSwitch } from "@halo-dev/components";
import { computed, type PropType } from "vue";

const { context } = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

function onChange(value) {
  context.node.input(
    value ? context.node.props.onValue : context.node.props.offValue
  );
}

const isChecked = computed(() => {
  return context.value === context.node.props.onValue;
});

const disabled = computed(() => {
  return context.node.props.disabled;
});
</script>

<template>
  <VSwitch
    :model-value="isChecked"
    :disabled="disabled"
    @update:model-value="onChange"
  ></VSwitch>
</template>
