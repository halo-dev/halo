<script lang="ts" setup>
import { VCard, VSpace } from "@halo-dev/components";
import { posts } from "@/modules/contents/posts/posts-mock";
</script>
<template>
  <VCard
    :bodyClass="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近发布"
  >
    <div class="h-full p-4">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(post, index) in posts"
          :key="index"
          class="cursor-pointer py-4 hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-gray-900">
                {{ post.title }}
              </p>
              <div class="mt-1 flex">
                <VSpace>
                  <span class="text-xs text-gray-500">
                    阅读 {{ post.visits }}
                  </span>
                  <span class="text-xs text-gray-500">
                    评论 {{ post.commentCount }}
                  </span>
                </VSpace>
              </div>
            </div>

            <div>
              <time class="text-sm text-gray-500" datetime="2020-01-07 20:00">
                2020-01-07 20:00
              </time>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </VCard>
</template>
