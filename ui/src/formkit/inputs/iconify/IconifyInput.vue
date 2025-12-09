<script setup lang="ts">
import { VButton, VDropdown } from "@halo-dev/components";
import { ref } from "vue";
import IconifyPicker from "./IconifyPicker.vue";

const selectedIcon = ref<string>();

const onSelect = (icon: string) => {
  selectedIcon.value = icon;
};
</script>

<template>
  <VDropdown class="inline-flex">
    <VButton>选择</VButton>
    <template #popper>
      <IconifyPicker @select="onSelect" />
    </template>
  </VDropdown>

  <div v-html="selectedIcon"></div>

  <div>{{ selectedIcon }}</div>
</template>

<style scoped></style>
