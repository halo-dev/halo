<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import Icons from "./Icons.vue";
import { iconifyClient } from "./api";
import type { IconifyValue } from "./types";

const emit = defineEmits<{
  (e: "select", icon: IconifyValue): void;
}>();

const keyword = ref<string>("");

const { data, isLoading } = useQuery({
  queryKey: ["iconify:icons", keyword],
  queryFn: async () => {
    if (!keyword.value) {
      return [];
    }
    const response = await iconifyClient
      .get<{ icons: string[] }>(`/search`, {
        params: {
          query: keyword.value,
          limit: 999,
        },
      })
      .then((res) => res.data);

    return response.icons;
  },
});

const onSelect = (icon: IconifyValue) => {
  emit("select", icon);
};
</script>
<template>
  <div class="flex h-[350px] flex-col gap-2 bg-white sm:h-[500px] sm:w-[500px]">
    <div class="flex-none">
      <SearchInput v-model="keyword" />
    </div>
    <div
      v-if="!keyword"
      class="flex items-center justify-center py-2 text-sm text-gray-600"
    >
      {{ $t("core.common.placeholder.search") }}
    </div>
    <VLoading v-else-if="isLoading" />
    <div
      v-else-if="!data?.length"
      class="flex items-center justify-center py-2 text-sm text-gray-600"
    >
      {{ $t("core.formkit.iconify.no_results") }}
    </div>
    <div
      v-else
      class="flex min-h-0 flex-1 shrink rounded-base border border-gray-200 p-1.5"
    >
      <Icons :icons="data" @select="onSelect" />
    </div>
  </div>
</template>
