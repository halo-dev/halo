<script setup lang="ts">
import { VTabItem, VTabs } from "@halo-dev/components";
import { provide, ref, toRefs, type Ref } from "vue";
import CollectionsView from "./CollectionsView.vue";
import SearchView from "./SearchView.vue";
import type { IconifyFormat } from "./types";

const props = defineProps<{
  format: IconifyFormat;
}>();

const { format } = toRefs(props);

provide<Ref<IconifyFormat>>("format", format);

const emit = defineEmits<{
  (e: "select", icon: string): void;
}>();

const activeTab = ref<"collections" | "search">("collections");

const onSelect = (icon: string) => {
  emit("select", icon);
};
</script>

<template>
  <div class="w-full">
    <VTabs v-model:active-id="activeTab" type="outline">
      <VTabItem id="collections" label="图标集">
        <CollectionsView @select="onSelect" />
      </VTabItem>
      <VTabItem id="search" label="全局搜索">
        <SearchView @select="onSelect" />
      </VTabItem>
    </VTabs>

    <div class="mt-2 flex justify-end text-xs text-gray-500">
      图标数据来自
      <a
        href="https://iconify.design"
        class="text-gray-900 hover:underline"
        rel="noopener noreferrer"
        target="_blank"
        >Iconify</a
      >
    </div>
  </div>
</template>

<style scoped></style>
