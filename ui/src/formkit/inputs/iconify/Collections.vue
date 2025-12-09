<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import type { IconifyInfo } from "@iconify/types";
import { Icon } from "@iconify/vue";
import { useQuery } from "@tanstack/vue-query";
import { useVirtualizer } from "@tanstack/vue-virtual";
import { useFuse, type UseFuseOptions } from "@vueuse/integrations/useFuse";
import { computed, ref, useTemplateRef, watch } from "vue";
import { iconifyClient } from "./api";

const ROW_HEIGHT = 80;
const ROW_GAP = 8;

const modelValue = defineModel<string | undefined>();

const container = useTemplateRef<HTMLDivElement>("container");

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

const rowVirtualizerOptions = computed(() => ({
  count: results.value.length,
  getScrollElement: () => container.value,
  estimateSize: () => ROW_HEIGHT + ROW_GAP,
  overscan: 5,
}));

const rowVirtualizer = useVirtualizer(rowVirtualizerOptions);

const virtualRows = computed(() => rowVirtualizer.value.getVirtualItems());
const totalSize = computed(() => rowVirtualizer.value.getTotalSize());

watch(
  () => results.value,
  (value) => {
    if (value.length) {
      modelValue.value = value[0].item.key;
    }
    rowVirtualizer.value.scrollToIndex(0);
  },
  {
    immediate: true,
  }
);
</script>
<template>
  <div class="flex-none">
    <SearchInput v-model="keyword" placeholder="搜索图标集合" sync />
  </div>
  <VLoading v-if="isLoading" />
  <div
    v-else
    ref="container"
    class="size-full min-h-0 flex-1 shrink overflow-auto"
  >
    <div
      :style="{
        height: `${totalSize}px`,
        width: '100%',
        position: 'relative',
      }"
    >
      <div
        v-for="virtualRow in virtualRows"
        :key="virtualRow.index"
        class="absolute left-0 flex w-full cursor-pointer items-center justify-between gap-2 rounded-base border p-2 transition-all"
        :class="{
          'border-primary': modelValue === results[virtualRow.index].item.key,
        }"
        :style="{
          height: `${ROW_HEIGHT}px`,
          transform: `translateY(${virtualRow.start}px)`,
        }"
        @click="modelValue = results[virtualRow.index].item.key"
      >
        <div>
          <div class="line-clamp-1 text-sm">
            {{ results[virtualRow.index].item.value.name }}
          </div>
          <div class="line-clamp-1 text-xs text-gray-600">
            {{ results[virtualRow.index].item.value.license.title }}
          </div>
          <div class="text-xs text-gray-600">
            {{ results[virtualRow.index].item.value.total }}
          </div>
        </div>

        <div class="hidden sm:grid sm:grid-cols-3 sm:gap-1">
          <Icon
            v-for="sample in results[virtualRow.index].item.value.samples"
            :key="sample"
            :icon="`${results[virtualRow.index].item.key}:${sample}`"
          />
        </div>
      </div>
    </div>
  </div>
</template>
