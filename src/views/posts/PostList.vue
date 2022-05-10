<script lang="ts" setup>
import FilledLayout from "@/layouts/FilledLayout.vue";
import { VButton } from "@/components/base/button";
import { VTag } from "@/components/base/tag";
import { IconBookRead } from "@/core/icons";
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
</script>
<template>
  <FilledLayout>
    <div class="flex items-center justify-between p-4 bg-white">
      <div class="flex-1 self-center min-w-0">
        <h2 class="flex text-xl font-bold text-gray-800 truncate">
          <IconBookRead class="self-center mr-2" />
          <span>文章</span>
        </h2>
      </div>
      <div class="self-center">
        <VButton type="secondary">发布</VButton>
      </div>
    </div>

    <div class="m-4">
      <div class="flex flex-col">
        <div class="overflow-x-auto">
          <div class="inline-block min-w-full py-2 align-middle">
            <div
              class="relative overflow-hidden shadow-sm"
              style="border-radius: 4px"
            >
              <div
                v-show="checkAll"
                class="absolute top-0 left-12 flex h-12 items-center space-x-3 bg-gray-50 sm:left-16"
              >
                <VButton size="sm" type="danger"> 删除 </VButton>
                <VButton size="sm" type="default"> 更多 </VButton>
              </div>

              <table class="min-w-full table-fixed divide-y divide-gray-300">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="relative w-12 px-6 sm:w-16 sm:px-8" scope="col">
                      <input
                        v-model="checkAll"
                        class="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-themeable-primary-600 focus:ring-themeable-primary-500 sm:left-6"
                        type="checkbox"
                        @change="handleCheckAll"
                      />
                    </th>
                    <th
                      class="min-w-[12rem] py-3.5 pr-3 text-left text-sm font-semibold text-gray-900"
                      scope="col"
                    >
                      标题
                    </th>
                    <th
                      class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900"
                      scope="col"
                    >
                      分类
                    </th>
                    <th
                      class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900"
                      scope="col"
                    >
                      标签
                    </th>
                    <th
                      class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900"
                      scope="col"
                    >
                      作者
                    </th>
                    <th
                      class="relative py-3.5 pl-3 pr-4 sm:pr-6 text-right"
                      scope="col"
                    >
                      操作
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200 bg-white">
                  <tr
                    v-for="(post, index) in postsRef"
                    :key="index"
                    class="cursor-pointer hover:bg-gray-100"
                  >
                    <td class="relative w-12 px-6 sm:w-16 sm:px-8">
                      <div
                        v-if="post.checked"
                        class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
                      ></div>

                      <input
                        v-model="post.checked"
                        class="absolute cursor-pointer left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-themeable-primary-600 focus:ring-themeable-primary-500 sm:left-6"
                        type="checkbox"
                      />
                    </td>
                    <td
                      class="whitespace-nowrap py-4 pr-3 text-sm font-medium text-gray-900"
                    >
                      <span class="mr-1 self-center">{{ post.title }}</span>
                      <VTag v-if="index === 0" class="self-center">草稿</VTag>

                      <dl class="font-normal lg:hidden">
                        <dt class="sr-only">Title</dt>
                        <dd class="mt-1 truncate text-gray-700">
                          Front-end Developer
                        </dd>
                        <dt class="sr-only sm:hidden">Email</dt>
                        <dd class="mt-1 truncate text-gray-500 sm:hidden">
                          lindsay.walton@example.com
                        </dd>
                      </dl>
                    </td>
                    <td
                      class="whitespace-nowrap px-3 py-4 text-sm text-gray-500"
                    >
                      分类
                    </td>
                    <td
                      class="whitespace-nowrap px-3 py-4 text-sm text-gray-500"
                    >
                      标签
                    </td>
                    <td
                      class="whitespace-nowrap px-3 py-4 text-sm text-gray-500"
                    >
                      作者
                    </td>
                    <td
                      class="whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6"
                    >
                      操作
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </FilledLayout>
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
