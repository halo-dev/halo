<script lang="ts" setup>
import { usePermission } from "@/utils/permission";

withDefaults(
  defineProps<{
    permissions: string[];
  }>(),
  {}
);

const { currentUserHasPermission } = usePermission();
</script>

<template>
  <slot v-if="currentUserHasPermission(permissions)" />
</template>
