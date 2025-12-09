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
      <VTabItem
        id="collections"
        :label="$t('core.formkit.iconify.tabs.collections')"
      >
        <CollectionsView @select="onSelect" />
      </VTabItem>
      <VTabItem id="search" :label="$t('core.formkit.iconify.tabs.search')">
        <SearchView @select="onSelect" />
      </VTabItem>
    </VTabs>

    <div class="mt-2 flex justify-end text-xs text-gray-500">
      <i18n-t keypath="core.formkit.iconify.copyright" tag="span">
        <template #url>
          <a
            href="https://iconify.design"
            class="text-gray-900 hover:underline"
            rel="noopener noreferrer"
            target="_blank"
          >
            Iconify
          </a>
        </template>
      </i18n-t>
    </div>
  </div>
</template>

<style scoped></style>
