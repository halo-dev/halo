<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { VTag } from "@/components/base/tag";
import { VTabItem, VTabs } from "@/components/base/tabs";
import { VInput } from "@/components/base/input";
import { VPageHeader } from "@/components/base/header";
import { IconArrowDown, IconPages, IconSettings } from "@/core/icons";
import { ref } from "vue";
import { users } from "@/views/system/users/users-mock";
import halo from "@/assets/logo-mock/halo.png";

const sheetsRef = ref([
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

const advancedSheets = ref([
  {
    name: "友情链接",
    author: "halo-dev",
    logo: halo,
    url: "/links",
  },
  {
    name: "图库",
    author: "halo-dev",
    logo: halo,
    url: "/photos",
  },
  {
    name: "社区",
    author: "halo-dev",
    logo: halo,
    url: "/community",
  },
]);

const checkAll = ref(false);
const activeId = ref("advanced");
</script>
<template>
  <VPageHeader title="页面">
    <template #icon>
      <IconPages class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton type="secondary">发布</VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VTabs v-model:active-id="activeId" type="outline">
      <VTabItem id="advanced" label="功能页面">
        <VCard :body-class="['!p-0']">
          <ul
            class="divide-y divide-gray-100 box-border w-full h-full"
            role="list"
          >
            <li v-for="(sheet, index) in advancedSheets" :key="index">
              <div
                class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all relative"
              >
                <div class="flex flex-row items-center relative">
                  <div class="flex-1">
                    <div class="flex flex-row">
                      <span
                        class="mr-0 sm:mr-2 text-sm font-medium truncate text-gray-900"
                      >
                        {{ sheet.name }}
                      </span>
                      <VTag>{{ sheet.url }}</VTag>
                    </div>
                  </div>
                  <div class="flex">
                    <div
                      class="inline-flex flex-col items-end gap-4 flex-col-reverse sm:flex-row sm:items-center sm:gap-6"
                    >
                      <img
                        v-tooltip="`由${sheet.name}插件提供`"
                        :src="sheet.logo"
                        class="hidden sm:inline-block h-6 w-6 rounded-full ring-2 ring-white"
                      />
                      <span class="cursor-pointer">
                        <IconSettings />
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </VCard>
      </VTabItem>
      <VTabItem id="custom" label="自定义页面">
        <VCard :body-class="['!p-0']">
          <template #header>
            <div class="px-4 py-3 block w-full bg-gray-50">
              <div
                class="flex flex-col sm:flex-row items-start sm:items-center relative"
              >
                <div class="hidden sm:flex items-center mr-4">
                  <input
                    v-model="checkAll"
                    class="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                    type="checkbox"
                  />
                </div>
                <div class="w-full sm:w-auto flex flex-1">
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
                <div class="mt-4 sm:mt-0 flex">
                  <VSpace spacing="lg">
                    <FloatingDropdown>
                      <div
                        class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm select-none"
                      >
                        <span class="mr-0.5">作者</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="p-4 w-80 h-96">
                          <div class="bg-white">
                            <!--TODO: Auto Focus-->
                            <VInput placeholder="根据关键词搜索"></VInput>
                          </div>
                          <div class="mt-2">
                            <ul class="divide-y divide-gray-200" role="list">
                              <li
                                v-for="(user, index) in users"
                                :key="index"
                                class="py-4 cursor-pointer hover:bg-gray-50"
                              >
                                <div class="flex items-center space-x-4">
                                  <div class="flex items-center">
                                    <input
                                      class="h-4 w-4 text-indigo-600 border-gray-300 rounded"
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
                                  <div class="flex-1 min-w-0">
                                    <p
                                      class="text-sm font-medium text-gray-900 truncate"
                                    >
                                      {{ user.name }}
                                    </p>
                                    <p class="text-sm text-gray-500 truncate">
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
                        class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm select-none"
                      >
                        <span class="mr-0.5">排序</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="p-4 w-72">
                          <ul class="space-y-1">
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
                            >
                              <span class="truncate">较近发布</span>
                            </li>
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
                            >
                              <span class="truncate">较晚发布</span>
                            </li>
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
                            >
                              <span class="truncate">浏览量最多</span>
                            </li>
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
                            >
                              <span class="truncate">浏览量最少</span>
                            </li>
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
                            >
                              <span class="truncate">评论量最多</span>
                            </li>
                            <li
                              class="cursor-pointer text-gray-600 hover:bg-gray-100 hover:text-gray-900 flex items-center px-3 py-2 text-sm rounded"
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
            class="divide-y divide-gray-100 box-border w-full h-full"
            role="list"
          >
            <li v-for="(sheet, index) in sheetsRef" :key="index">
              <div
                :class="{
                  'bg-gray-100': checkAll,
                }"
                class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all relative"
              >
                <div
                  v-show="checkAll"
                  class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
                ></div>
                <div class="flex flex-row items-center relative">
                  <div class="hidden mr-4 sm:flex items-center">
                    <input
                      v-model="checkAll"
                      class="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                      type="checkbox"
                    />
                  </div>
                  <div class="flex-1">
                    <div class="flex flex-row">
                      <span
                        class="mr-0 sm:mr-2 text-sm font-medium truncate text-gray-900"
                      >
                        {{ sheet.title }}
                      </span>
                      <VTag>{{ sheet.url }}</VTag>
                    </div>
                    <div class="flex mt-1">
                      <VSpace>
                        <span class="text-xs text-gray-500">
                          访问量 {{ sheet.views }}
                        </span>
                        <span class="text-xs text-gray-500">
                          评论 {{ sheet.commentCount }}
                        </span>
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
            <div class="bg-white flex items-center justify-end">
              <div class="flex-1 flex items-center justify-end">
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
                    <span
                      class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700"
                    >
                      ...
                    </span>
                    <a
                      class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 hidden md:inline-flex relative items-center px-4 py-2 border text-sm font-medium"
                      href="#"
                    >
                      4
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
      </VTabItem>
    </VTabs>
  </div>
</template>
