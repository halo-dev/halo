<script lang="ts" name="PostStatsWidget" setup>
import { VCard } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { apiClient } from "@halo-dev/admin-shared";

const postTotal = ref<number>(0);

const handleFetchPosts = async () => {
  const { data } =
    await apiClient.extension.post.listcontentHaloRunV1alpha1Post();
  postTotal.value = data.total;
};

onMounted(handleFetchPosts);
</script>
<template>
  <VCard class="h-full">
    <dt class="truncate text-sm font-medium text-gray-500">文章</dt>
    <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ postTotal }}</dd>
  </VCard>
</template>
