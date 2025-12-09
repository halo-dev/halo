<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import type { IconifyInfo } from "@iconify/types";
import { Icon } from "@iconify/vue";
import { useQuery } from "@tanstack/vue-query";
import { useFuse, type UseFuseOptions } from "@vueuse/integrations/useFuse";
import { computed, ref, watch } from "vue";
import iconifyClient from "./api";

const modelValue = defineModel<string | undefined>();

const { data, isLoading } = useQuery({
  queryKey: ["iconify:collections"],
  queryFn: () =>
    iconifyClient
      .get<Record<string, IconifyInfo>>("/collections")
      .then((res) => res.data),
});

const collections = computed(() => {
  return Object.entries(data.value || {}).map(([key, value]) => ({
    key,
    value,
  }));
});

const keyword = ref<string>("");

const fuseOptions: UseFuseOptions<{
  key: string;
  value: IconifyInfo;
}> = {
  matchAllWhenSearchEmpty: true,
  fuseOptions: {
    keys: ["key", "value.name"],
  },
};

const { results } = useFuse(keyword, collections, fuseOptions);

watch(
  () => results.value,
  (value) => {
    if (value.length) {
      modelValue.value = value[0].item.key;
    }
  },
  {
    immediate: true,
  }
);
</script>
<template>
  <SearchInput v-model="keyword" placeholder="搜索图标集合" sync />
  <VLoading v-if="isLoading" />
  <div
    v-for="result in results"
    v-else
    :key="result.item.key"
    class="flex cursor-pointer items-center justify-between gap-2 rounded-base border p-2 transition-all"
    :class="{
      'border-primary': modelValue === result.item.key,
    }"
    @click="modelValue = result.item.key"
  >
    <div>
      <div class="line-clamp-1 text-sm">
        {{ result.item.value.name }}
      </div>
      <div class="line-clamp-1 text-xs text-gray-600">
        {{ result.item.value.license.title }}
      </div>
      <div class="text-xs text-gray-600">
        {{ result.item.value.total }}
      </div>
    </div>

    <div class="grid grid-cols-3 gap-1">
      <Icon
        v-for="sample in result.item.value.samples"
        :key="sample"
        :icon="`${result.item.key}:${sample}`"
      />
    </div>
  </div>
</template>
