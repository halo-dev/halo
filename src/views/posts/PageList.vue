<script lang="ts" setup>
import BasicLayout from "@/layouts/BasicLayout.vue";
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { VTag } from "@/components/base/tag";
import { VInput } from "@/components/base/input";
import { VPageHeader } from "@/components/base/header";
import { IconPages, IconSettings } from "@/core/icons";
import { posts } from "./posts-mock";
import { ref } from "vue";

const postsRef = ref(
  posts.map((item) => {
    return {
      ...item,
      checked: false,
    };
  })
);

const checkAll = ref(false);

const handleCheckAll = () => {
  postsRef.value.forEach((item) => {
    item.checked = checkAll.value;
  });
};
handleCheckAll();
</script>
<template>
  <BasicLayout>
    <VPageHeader title="页面">
      <template #icon>
        <IconPages class="self-center mr-2" />
      </template>
      <template #actions>
        <VButton type="secondary">发布</VButton>
      </template>
    </VPageHeader>

    <div class="m-0 md:m-4">
      <VCard :body-class="['!p-0']">
        <template #header>
          <div
            class="flex flex-col w-full p-4 items-stretch sm:flex-row sm:items-center"
          >
            <div class="flex-1">
              <VInput placeholder="输入关键词搜索" />
            </div>
            <div class="flex flex-row gap-3">
              <div>分类</div>
              <div>标签</div>
              <div>作者</div>
              <div>排序</div>
            </div>
          </div>
        </template>
        <ul
          class="divide-y divide-gray-100 box-border w-full h-full"
          role="list"
        >
          <li v-for="(post, index) in postsRef" :key="index">
            <div
              class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all"
            >
              <div class="flex flex-row items-center relative">
                <div class="flex-1">
                  <div class="flex flex-col sm:flex-row">
                    <span
                      class="mr-0 sm:mr-2 text-sm font-medium truncate text-gray-900"
                    >
                      {{ post.title }}
                    </span>
                    <VSpace class="mt-1 sm:mt-0">
                      <VTag
                        v-for="(tag, tagIndex) in post.tags"
                        :key="tagIndex"
                      >
                        {{ tag.name }}
                      </VTag>
                    </VSpace>
                  </div>
                  <div class="flex mt-1">
                    <VSpace>
                      <span class="text-xs text-gray-500"
                        >阅读 {{ post.visits }}</span
                      >
                      <span class="text-xs text-gray-500"
                        >评论 {{ post.commentCount }}</span
                      >
                    </VSpace>
                  </div>
                </div>
                <div class="flex">
                  <div
                    class="inline-flex flex-col items-end gap-4 flex-col-reverse sm:flex-row sm:items-center sm:gap-6"
                  >
                    <img
                      class="hidden sm:inline-block h-6 w-6 rounded-full ring-2 ring-white"
                      src="https://ryanc.cc/avatar"
                    />
                    <time class="text-sm text-gray-500" datetime="2020-01-07">
                      2020-01-07
                    </time>
                    <span>
                      <IconSettings />
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </li>
        </ul>

        <template #footer>
          <div class="bg-white flex items-center justify-between">
            <div class="flex-1 flex justify-between sm:hidden">
              <a
                class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                href="#"
              >
                Previous
              </a>
              <a
                class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                href="#"
              >
                Next
              </a>
            </div>
            <div
              class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between"
            >
              <div>
                <p class="text-sm text-gray-700">
                  Showing
                  <span class="font-medium">1</span>
                  to
                  <span class="font-medium">10</span>
                  of
                  <span class="font-medium">97</span>
                  results
                </p>
              </div>
              <div>
                <nav
                  aria-label="Pagination"
                  class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
                >
                  <a
                    class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    href="#"
                  >
                    <span class="sr-only">Previous</span>
                    <svg
                      aria-hidden="true"
                      class="h-5 w-5"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        clip-rule="evenodd"
                        d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                        fill-rule="evenodd"
                      />
                    </svg>
                  </a>
                  <a
                    aria-current="page"
                    class="z-10 bg-indigo-50 border-indigo-500 text-indigo-600 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    1
                  </a>
                  <a
                    class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    2
                  </a>
                  <a
                    class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 hidden md:inline-flex relative items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    3
                  </a>
                  <span
                    class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700"
                  >
                    ...
                  </span>
                  <a
                    class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 hidden md:inline-flex relative items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    8
                  </a>
                  <a
                    class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    9
                  </a>
                  <a
                    class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    href="#"
                  >
                    10
                  </a>
                  <a
                    class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    href="#"
                  >
                    <span class="sr-only">Next</span>
                    <svg
                      aria-hidden="true"
                      class="h-5 w-5"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        clip-rule="evenodd"
                        d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                        fill-rule="evenodd"
                      />
                    </svg>
                  </a>
                </nav>
              </div>
            </div>
          </div>
        </template>
      </VCard>
    </div>
  </BasicLayout>
</template>

<style lang="scss">
.table-container {
  @apply table;
  @apply w-full;
  border: 1px solid #fff;
  background: #fff;

  border-radius: 4px;

  .table-item {
    @apply table-row;
    @apply cursor-pointer;
    @apply transition-all;

    &:hover {
      @apply bg-gray-100;
    }
  }

  .table-cell {
    display: table-cell;
    padding: 10px;
  }
}
</style>
