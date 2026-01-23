<script lang="ts" setup>
import type { ToolbarItemComponentProps } from "@/types";
import DropdownItem from "../base/DropdownItem.vue";

const props = defineProps<ToolbarItemComponentProps>();

const action = () => {
  if (props.disabled) return;
  props.action?.();
};
</script>

<template>
  <DropdownItem :disabled="disabled" :is-active="isActive" @click="action">
    <template v-if="icon" #icon>
      <component :is="icon" />
    </template>
    {{ title }}
  </DropdownItem>
</template>
