<script lang="ts" name="CommentStatsWidget" setup>
import { apiClient } from "@/utils/api-client";
import { VCard } from "@halo-dev/components";
import { onMounted, ref } from "vue";

const commentTotal = ref<number>(0);

const handleFetchComments = async () => {
  const { data } =
    await apiClient.extension.comment.listcontentHaloRunV1alpha1Comment();
  commentTotal.value = data.total;
};

onMounted(handleFetchComments);
</script>
<template>
  <VCard class="h-full">
    <dt class="truncate text-sm font-medium text-gray-500">评论</dt>
    <dd class="mt-1 text-3xl font-semibold text-gray-900">
      {{ commentTotal }}
    </dd>
  </VCard>
</template>
