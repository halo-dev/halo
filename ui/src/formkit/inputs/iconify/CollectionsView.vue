<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useFuse } from "@vueuse/integrations/useFuse.mjs";
import { flatten } from "es-toolkit";
import { values } from "es-toolkit/compat";
import { computed, ref } from "vue";
import Collections from "./Collections.vue";
import Icons from "./Icons.vue";
import { iconifyClient } from "./api";
import type { IconifyValue } from "./types";

const emit = defineEmits<{
  (e: "select", icon: IconifyValue): void;
}>();

const selectedCollection = ref<string>();
const keyword = ref<string>("");

const { data, isLoading } = useQuery({
  queryKey: ["iconify:icons", selectedCollection],
  queryFn: () =>
    iconifyClient
      .get(`/collection`, {
        params: {
          prefix: selectedCollection.value,
          chars: true,
        },
      })
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

const onSelect = (icon: IconifyValue) => {
  emit("select", icon);
};
</script>
<template>
  <div
    class="flex h-[350px] divide-x rounded-base border border-gray-200 bg-white sm:h-[500px] sm:w-[700px]"
  >
    <div
      class="flex h-full w-28 flex-none flex-col gap-2 overflow-auto p-1.5 sm:w-56"
    >
      <Collections v-model="selectedCollection" />
    </div>
    <div class="flex h-full flex-1 flex-col gap-2 p-1.5">
      <SearchInput
        v-model="keyword"
        :placeholder="$t('core.formkit.iconify.search_placeholder')"
        sync
      />
      <VLoading v-if="isLoading" />
      <Icons
        v-else
        :icons="results.map((result) => result.item)"
        @select="onSelect"
      />
    </div>
  </div>
</template>
