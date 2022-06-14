<script lang="ts" setup>
import {
  IconArrowDown,
  IconMessage,
  IconSettings,
  VButton,
  VCard,
  VInput,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { ref } from "vue";

const checkAll = ref(false);
</script>
<template>
  <VPageHeader title="评论">
    <template #icon>
      <IconMessage class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="default">回收站</VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="mr-4 hidden items-center sm:flex">
              <input
                v-model="checkAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
              />
            </div>
            <div class="flex w-full flex-1 sm:w-auto">
              <VInput
                v-if="!checkAll"
                class="w-full sm:w-72"
                placeholder="输入关键词搜索"
              />
              <VSpace v-else>
                <VButton type="default">设置</VButton>
                <VButton type="danger">删除</VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <div
                  class="flex cursor-pointer items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5">状态</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <div
                  class="flex cursor-pointer items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5">评论者</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <div
                  class="flex cursor-pointer items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5">排序</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="index in 10" :key="index">
          <div
            :class="{
              'bg-gray-100': checkAll,
            }"
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              v-show="checkAll"
              class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
            ></div>
            <div class="relative flex flex-row items-center">
              <div class="mr-4 hidden items-center sm:flex">
                <input
                  v-model="checkAll"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  type="checkbox"
                />
              </div>
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <span
                    class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                  >
                    Ryan Wang
                  </span>
                </div>
                <div class="mt-1 flex">
                  <VSpace>
                    <p class="text-xs text-gray-500">评论测试</p>
                  </VSpace>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                >
                  <img
                    class="hidden h-6 w-6 rounded-full ring-2 ring-white sm:inline-block"
                    src="https://ryanc.cc/avatar"
                  />
                  <time class="text-sm text-gray-500" datetime="2020-01-07">
                    2020-01-07
                  </time>
                  <span class="cursor-pointer">
                    <IconSettings />
                  </span>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>

      <template #footer>
        <div class="flex items-center justify-end bg-white">
          <div class="flex flex-1 items-center justify-end">
            <div>
              <nav
                aria-label="Pagination"
                class="relative z-0 inline-flex -space-x-px rounded-md shadow-sm"
              >
                <a
                  class="relative inline-flex items-center rounded-l-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
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
                  class="relative z-10 inline-flex items-center border border-indigo-500 bg-indigo-50 px-4 py-2 text-sm font-medium text-indigo-600"
                  href="#"
                >
                  1
                </a>
                <a
                  class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                  href="#"
                >
                  2
                </a>
                <span
                  class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700"
                >
                  ...
                </span>
                <a
                  class="relative hidden items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 md:inline-flex"
                  href="#"
                >
                  4
                </a>
                <a
                  class="relative inline-flex items-center rounded-r-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
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
</template>
