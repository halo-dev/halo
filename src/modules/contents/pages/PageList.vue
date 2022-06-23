<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconPages,
  IconSettings,
  VButton,
  VCard,
  VInput,
  VPageHeader,
  VSpace,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { ref } from "vue";
import { users } from "@/modules/system/users/users-mock";
import type { PagesPublicState } from "@halo-dev/admin-shared";
import { useExtensionPointsState } from "@/composables/usePlugins";

const pagesRef = ref([
  {
    title: "关于我们",
    url: "/about",
    views: "31231",
    commentCount: "32",
  },
  {
    title: "案例中心",
    url: "/case",
    views: "11431",
    commentCount: "35",
  },
  {
    title: "我们的产品",
    url: "/products",
    views: "11431",
    commentCount: "35",
  },
]);

const activeId = ref("functional");
const checkAll = ref(false);
const pagesPublicState = ref<PagesPublicState>({
  functionalPages: [],
});

useExtensionPointsState("PAGES", pagesPublicState);
</script>
<template>
  <VPageHeader title="页面">
    <template #icon>
      <IconPages class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary">
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        新建
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeId"
          :items="[
            { id: 'functional', label: '功能页面' },
            { id: 'custom', label: '自定义页面' },
          ]"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div v-if="activeId === 'functional'">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li
            v-for="(page, index) in pagesPublicState.functionalPages"
            :key="index"
            @click="$router.push({ path: page.path })"
          >
            <div
              class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
            >
              <div class="relative flex flex-row items-center">
                <div class="flex-1">
                  <div class="flex flex-row">
                    <span
                      class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                    >
                      {{ page.name }}
                    </span>
                    <VTag>{{ page.url }}</VTag>
                  </div>
                </div>
                <div class="flex">
                  <div
                    class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                  >
                    <span class="cursor-pointer">
                      <IconSettings />
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <div v-if="activeId === 'custom'">
        <VCard
          :body-class="['!p-0']"
          class="rounded-none border-none shadow-none"
        >
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
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">作者</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="h-96 w-80 p-4">
                          <div class="bg-white">
                            <!--TODO: Auto Focus-->
                            <VInput placeholder="根据关键词搜索"></VInput>
                          </div>
                          <div class="mt-2">
                            <ul class="divide-y divide-gray-200" role="list">
                              <li
                                v-for="(user, index) in users"
                                :key="index"
                                class="cursor-pointer py-4 hover:bg-gray-50"
                              >
                                <div class="flex items-center space-x-4">
                                  <div class="flex items-center">
                                    <input
                                      class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                                      type="checkbox"
                                    />
                                  </div>
                                  <div class="flex-shrink-0">
                                    <img
                                      :alt="user.name"
                                      :src="user.avatar"
                                      class="h-10 w-10 rounded"
                                    />
                                  </div>
                                  <div class="min-w-0 flex-1">
                                    <p
                                      class="truncate text-sm font-medium text-gray-900"
                                    >
                                      {{ user.name }}
                                    </p>
                                    <p class="truncate text-sm text-gray-500">
                                      @{{ user.username }}
                                    </p>
                                  </div>
                                  <div>
                                    <VTag>{{ index + 1 }} 篇</VTag>
                                  </div>
                                </div>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">排序</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较近发布</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较晚发布</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">浏览量最多</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">浏览量最少</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">评论量最多</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">评论量最少</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li v-for="(page, index) in pagesRef" :key="index">
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
                    <div class="flex flex-row">
                      <span
                        class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                      >
                        {{ page.title }}
                      </span>
                      <VTag>{{ page.url }}</VTag>
                    </div>
                    <div class="mt-1 flex">
                      <VSpace>
                        <span class="text-xs text-gray-500">
                          访问量 {{ page.views }}
                        </span>
                        <span class="text-xs text-gray-500">
                          评论 {{ page.commentCount }}
                        </span>
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
    </VCard>
  </div>
</template>
