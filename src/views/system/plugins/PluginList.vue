<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { VInput } from "@/components/base/input";
import { VSpace } from "@/components/base/space";
import { VTag } from "@/components/base/tag";
import {
  IconAddCircle,
  IconArrowDown,
  IconPlug,
  IconSettings,
} from "@/core/icons";
import { ref } from "vue";
import { useRouter } from "vue-router";
import { plugins } from "./plugins-mock";

const checkAll = ref(false);

const router = useRouter();

// eslint-disable-next-line
const handleRouteToDetail = (plugin: any) => {
  router.push({
    name: "PluginDetail",
    params: { id: plugin.spec.pluginClass },
  });
};
</script>
<template>
  <VPageHeader title="插件">
    <template #icon>
      <IconPlug class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton type="secondary">
        <template #icon>
          <IconAddCircle class="w-full h-full" />
        </template>
        安装
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
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
                <VButton type="default">禁用</VButton>
                <VButton type="danger">卸载</VButton>
              </VSpace>
            </div>
            <div class="mt-4 sm:mt-0 flex">
              <VSpace spacing="lg">
                <div
                  class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                >
                  <span class="mr-0.5">启用状态</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <div
                  class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                >
                  <span class="mr-0.5">类别</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <div
                  class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                >
                  <span class="mr-0.5">提供方</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <div
                  class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
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
      <ul class="divide-y divide-gray-100 box-border w-full h-full" role="list">
        <li
          v-for="(plugin, index) in plugins"
          :key="index"
          @click.stop="handleRouteToDetail(plugin)"
        >
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
              <div v-if="plugin.spec.logo" class="mr-4">
                <div
                  class="w-12 h-12 border rounded p-1 hover:shadow-sm bg-white"
                >
                  <img
                    :alt="plugin.metadata.name"
                    :src="plugin.spec.logo"
                    class="w-full h-full"
                  />
                </div>
              </div>
              <div class="flex-1">
                <div class="flex flex-row items-center">
                  <span class="mr-2 text-sm font-medium truncate text-gray-900">
                    {{ plugin.metadata.name }}
                  </span>
                  <VSpace>
                    <VTag>
                      {{ plugin.metadata.enabled ? "已启用" : "未启用" }}
                    </VTag>
                  </VSpace>
                </div>
                <div class="flex mt-2">
                  <VSpace align="start" direction="column" spacing="xs">
                    <span class="text-xs text-gray-500">
                      {{ plugin.spec.description }}
                    </span>
                    <span class="sm:hidden text-xs text-gray-500">
                      @{{ plugin.spec.author }} {{ plugin.spec.version }}
                    </span>
                  </VSpace>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col items-end gap-4 flex-col-reverse sm:flex-row sm:items-center sm:gap-6"
                >
                  <a
                    :href="plugin.spec.homepage"
                    class="hidden sm:block text-sm text-gray-500 hover:text-gray-900"
                    target="_blank"
                  >
                    @{{ plugin.spec.author }}
                  </a>
                  <span class="hidden sm:block text-sm text-gray-500">
                    {{ plugin.spec.version }}
                  </span>
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
  </div>
</template>
