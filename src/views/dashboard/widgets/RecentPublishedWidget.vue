<script lang="ts" setup>
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { posts } from "@/views/contents/posts/posts-mock";
</script>
<template>
  <VCard
    :bodyClass="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近发布"
  >
    <div class="p-4 h-full">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(post, index) in posts"
          :key="index"
          class="py-4 cursor-pointer hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">
                {{ post.title }}
              </p>
              <div class="flex mt-1">
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
