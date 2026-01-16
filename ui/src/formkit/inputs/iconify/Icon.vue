<script lang="ts" setup>
import { VDropdown } from "@halo-dev/components";
import { Icon as IconifyIcon } from "@iconify/vue";
import { useTemplateRef } from "vue";
import IconConfirmPanel from "./IconConfirmPanel.vue";
import type { IconifyValue } from "./types";

defineProps<{
  iconName: string;
}>();

const emit = defineEmits<{
  (e: "select", icon: IconifyValue): void;
}>();

const dropdown = useTemplateRef<InstanceType<typeof VDropdown>>("dropdown");

function onSelect(icon: IconifyValue) {
  emit("select", icon);
  dropdown.value?.hide();
}
</script>
<template>
  <VDropdown ref="dropdown" class="inline-flex">
    <button
      v-tooltip="iconName"
      type="button"
      class="inline-flex size-full items-center justify-center rounded-lg hover:bg-gray-100 active:bg-gray-200"
      :aria-label="`Select icon: ${iconName}`"
    >
      <IconifyIcon :icon="iconName" class="text-2xl" />
    </button>
    <template #popper>
      <IconConfirmPanel :icon-name="iconName" @select="onSelect" />
    </template>
  </VDropdown>
</template>
