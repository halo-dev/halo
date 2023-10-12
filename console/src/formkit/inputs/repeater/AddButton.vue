<script lang="ts" setup>
import { VButton, IconAddCircle } from "@halo-dev/components";
import type { FormKitFrameworkContext } from "@formkit/core";
import type { PropType } from "vue";

defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
  disabled: {
    type: Boolean,
    required: false,
  },
});
</script>

<template>
  <div :class="context.classes.add">
    <VButton :disabled="disabled" type="secondary">
      <template #icon>
        <IconAddCircle class="h-full w-full" />
      </template>
      {{ context.addLabel || $t("core.common.buttons.add") }}
    </VButton>
  </div>
</template>
