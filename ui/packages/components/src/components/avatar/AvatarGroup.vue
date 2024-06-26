<script lang="ts" setup>
import { provide } from "vue";
import {
  AvatarGroupContextInjectionKey,
  type AvatarGroupProps,
} from "./interface";

const props = withDefaults(defineProps<AvatarGroupProps>(), {
  size: "md",
  circle: false,
});

provide(AvatarGroupContextInjectionKey, props);
</script>

<template>
  <div class="avatar-group-wrapper">
    <slot />
  </div>
</template>

<style lang="scss">
.avatar-group-wrapper {
  @apply -space-x-2.5 inline-flex;

  > * {
    @apply hover:z-10 ring-2 ring-white transition-all;
  }
}
</style>
