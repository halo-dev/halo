<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import Icons from "./Icons.vue";
import { iconifyClient } from "./api";

const emit = defineEmits<{
  (e: "select", iconName: string): void;
}>();

const keyword = ref<string>("");

const { data, isLoading } = useQuery({
  queryKey: ["iconify:icons", keyword],
  queryFn: async () => {
    if (!keyword.value) {
      return [];
    }
    const response = await iconifyClient
      .get<{ icons: string[] }>(`/search?query=${keyword.value}&limit=999`)
      .then((res) => res.data);

    return response.icons;
  },
});

const onSelect = (icon: string) => {
  emit("select", icon);
};
</script>
<template>
  <div class="flex h-[500px] flex-col gap-2 bg-white sm:w-[500px]">
    <div class="flex-none">
      <SearchInput v-model="keyword" />
    </div>
    <div
      v-if="!keyword"
      class="flex items-center justify-center py-2 text-sm text-gray-600"
    >
      请输入关键词进行搜索
    </div>
    <VLoading v-else-if="isLoading" />
    <div
      v-else-if="!data?.length"
      class="flex items-center justify-center py-2 text-sm text-gray-600"
    >
      没有找到结果，请尝试其他关键词
    </div>
    <div
      v-else
      class="flex min-h-0 flex-1 shrink rounded-base border border-gray-200 p-1.5"
    >
      <Icons :icons="data" @select="onSelect" />
    </div>
  </div>
</template>
