<script lang="ts" setup>
import { useQuery } from "@tanstack/vue-query";
import { useFuse } from "@vueuse/integrations/useFuse.mjs";
import { flatten } from "es-toolkit";
import { values } from "es-toolkit/compat";
import { computed, ref } from "vue";
import iconifyClient from "./api";
import Collections from "./Collections.vue";
import Icons from "./Icons.vue";

const emit = defineEmits<{
  (e: "select", iconName: string): void;
}>();

const selectedCollection = ref<string>();
const keyword = ref<string>("");

const { data } = useQuery({
  queryKey: ["iconify:icons", selectedCollection],
  queryFn: () =>
    iconifyClient
      .get(`/collection?prefix=${selectedCollection.value}&chars=true`)
      .then((res) => res.data),
  enabled: computed(() => !!selectedCollection.value),
});

const icons = computed(() => {
  return [
    ...flatten(values(data.value?.categories)),
    ...(data.value?.uncategorized || []),
  ].map((icon) => `${selectedCollection.value}:${icon}`);
});

const { results } = useFuse(keyword, icons, {
  matchAllWhenSearchEmpty: true,
});

const onSelect = (icon: string) => {
  emit("select", icon);
};
</script>
<template>
  <div
    class="flex h-[500px] w-[700px] divide-x rounded-base border border-gray-200 bg-white"
  >
    <div class="flex h-full w-56 flex-none flex-col gap-2 overflow-auto p-1.5">
      <Collections v-model="selectedCollection" />
    </div>
    <div class="flex h-full flex-1 flex-col gap-2 p-1.5">
      <SearchInput v-model="keyword" placeholder="搜索图标" sync />
      <Icons :icons="results.map((result) => result.item)" @select="onSelect" />
    </div>
  </div>
</template>
