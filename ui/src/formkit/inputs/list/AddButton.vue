<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconAddCircle, VButton } from "@halo-dev/components";
import type { PropType } from "vue";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
  disabled: {
    type: Boolean,
    required: false,
  },
  onClick: {
    type: Function as PropType<() => void>,
    required: true,
  },
});

const handleAppendClick = () => {
  if (!props.disabled && props.onClick) {
    props.onClick();
  }
};
</script>

<template>
  <div :class="context.classes.add" @click="handleAppendClick">
    <VButton :disabled="disabled" type="secondary">
      <template #icon>
        <IconAddCircle class="h-full w-full" />
      </template>
      {{ context.addLabel || $t("core.common.buttons.add") }}
    </VButton>
  </div>
</template>
