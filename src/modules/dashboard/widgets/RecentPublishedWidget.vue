<script lang="ts" name="RecentPublishedWidget" setup>
import { VCard, VSpace } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import type { ListedPost } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";

const posts = ref<ListedPost[]>([] as ListedPost[]);

const handleFetchPosts = async () => {
  try {
    const { data } = await apiClient.post.listPosts({
      sort: "PUBLISH_TIME",
      publishPhase: "PUBLISHED",
      page: 1,
      size: 10,
    });
    posts.value = data.items;
  } catch (e) {
    console.error("Failed to fetch posts", e);
  }
};

onMounted(handleFetchPosts);
</script>
<template>
  <VCard
    :body-class="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近文章"
  >
    <div class="h-full">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(post, index) in posts"
          :key="index"
          class="cursor-pointer p-4 hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-gray-900">
                {{ post.post.spec.title }}
              </p>
              <div class="mt-1 flex">
                <VSpace>
                  <span class="text-xs text-gray-500"> 阅读 0 </span>
                  <span class="text-xs text-gray-500"> 评论 0 </span>
                </VSpace>
              </div>
            </div>

            <div>
              <time class="text-sm tabular-nums text-gray-500">
                {{ formatDatetime(post.post.spec.publishTime) }}
              </time>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </VCard>
</template>
