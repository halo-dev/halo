<script setup lang="ts">
import { VTabItem, VTabs } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref, watch } from "vue";
import CollectionsView from "./CollectionsView.vue";
import SearchView from "./SearchView.vue";
import iconifyClient from "./api";

const emit = defineEmits<{
  (e: "select", icon: string): void;
}>();

const activeTab = ref<"collections" | "search">("collections");

const selectedIconName = ref<string>();

const { data: icon } = useQuery({
  queryKey: ["iconify:icon", selectedIconName],
  queryFn: () =>
    iconifyClient
      .get(
        `/${selectedIconName.value?.replace(":", "/")}.svg?width=200&color=red`
      )
      .then((res) => res.data),
  enabled: computed(() => !!selectedIconName.value),
});

watch(
  () => icon.value,
  (value) => {
    if (value) {
      emit("select", icon.value);
    }
  },
  {
    immediate: true,
  }
);

const onSelect = (iconName: string) => {
  selectedIconName.value = iconName;
};
</script>

<template>
  <VTabs v-model:active-id="activeTab" type="outline">
    <VTabItem id="collections" label="图标集">
      <CollectionsView @select="onSelect" />
    </VTabItem>
    <VTabItem id="search" label="全局搜索">
      <SearchView @select="onSelect" />
    </VTabItem>
  </VTabs>
</template>

<style scoped></style>
