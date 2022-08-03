<script lang="ts" setup>
import {
  IconExchange,
  IconEye,
  IconPalette,
  VAlert,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import ThemeListModal from "./components/ThemeListModal.vue";
import { ref } from "vue";
import { RouterLink } from "vue-router";
import type { Theme } from "@halo-dev/api-client";
import { useThemeLifeCycle } from "./composables/use-theme";

const selectedTheme = ref<Theme>({} as Theme);
const themesModal = ref(false);
const tabActiveId = ref("detail");
const themeListRef = ref();

const { isActivated, activatedTheme, handleActiveTheme } =
  useThemeLifeCycle(selectedTheme);
</script>

<template>
  <ThemeListModal
    ref="themeListRef"
    v-model:activated-theme="activatedTheme"
    v-model:selected-theme="selectedTheme"
    v-model:visible="themesModal"
  />
  <VPageHeader :title="selectedTheme.spec?.displayName">
    <template #icon>
      <IconPalette class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" type="default" @click="themesModal = true">
          <template #icon>
            <IconExchange class="h-full w-full" />
          </template>
          切换主题
        </VButton>
        <VButton
          v-if="!isActivated"
          size="sm"
          type="primary"
          @click="handleActiveTheme"
        >
          启用
        </VButton>
        <VButton :route="{ name: 'ThemeVisual' }" type="secondary">
          <template #icon>
            <IconEye class="h-full w-full" />
          </template>
          可视化编辑
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="tabActiveId"
          :items="[
            { id: 'detail', label: '详情' },
            { id: 'settings', label: '基础设置' },
          ]"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>

      <div v-if="tabActiveId === 'detail'">
        <div class="px-4 py-4 sm:px-6">
          <div class="flex flex-row gap-3">
            <div v-if="selectedTheme.spec?.logo">
              <div
                class="h-12 w-12 overflow-hidden rounded border bg-white hover:shadow-sm"
              >
                <img
                  :alt="selectedTheme.spec?.displayName"
                  :src="selectedTheme.spec?.logo"
                  class="h-full w-full"
                />
              </div>
            </div>
            <div>
              <h3 class="text-lg font-medium leading-6 text-gray-900">
                {{ selectedTheme.spec?.displayName }}
              </h3>
              <p class="mt-1 flex max-w-2xl items-center gap-2">
                <span class="text-sm text-gray-500">
                  {{ selectedTheme.spec?.version }}
                </span>
                <VTag>
                  {{ isActivated ? "当前启用" : "未启用" }}
                </VTag>
              </p>
            </div>
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">ID</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ selectedTheme.metadata?.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">作者</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ selectedTheme.spec?.author?.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">网站</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="selectedTheme.spec?.website" target="_blank">
                  {{ selectedTheme.spec?.website }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">源码仓库</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="selectedTheme.spec?.repo" target="_blank">
                  {{ selectedTheme.spec?.repo }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">当前版本</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ selectedTheme.spec?.version }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ selectedTheme.spec?.require }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">存储位置</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                无
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">插件依赖</dt>
              <dd class="mt-1 text-sm sm:col-span-3 sm:mt-0">
                <VAlert
                  description="当前有 1 个插件还未安装"
                  title="提示"
                ></VAlert>
                <ul class="mt-2 space-y-2">
                  <li>
                    <div
                      class="inline-flex w-96 cursor-pointer flex-row flex-col gap-y-3 rounded border p-5 hover:border-primary"
                    >
                      <RouterLink
                        :to="{
                          name: 'PluginDetail',
                          params: { name: 'PluginLinks' },
                        }"
                        class="font-medium text-gray-900 hover:text-blue-400"
                      >
                        run.halo.plugins.links
                      </RouterLink>
                      <div class="text-xs">
                        <VSpace>
                          <VTag> 已安装</VTag>
                        </VSpace>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-96 cursor-pointer flex-row flex-col gap-y-3 rounded border p-5 hover:border-primary"
                    >
                      <span class="font-medium hover:text-blue-400">
                        run.halo.plugins.photos
                      </span>
                      <div class="text-xs">
                        <VSpace>
                          <VTag>未安装</VTag>
                        </VSpace>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <div v-if="tabActiveId === 'settings'" class="p-4 sm:px-6">
        <div class="w-1/3">
          <FormKit id="theme-setting-form" :actions="false" type="form">
            <FormKit label="侧边栏宽度" type="text"></FormKit>
            <FormKit label="侧边栏背景图" type="text"></FormKit>
            <FormKit label="右上角图标" type="text"></FormKit>
            <FormKit label="文章代码高亮语言" type="text"></FormKit>
          </FormKit>
        </div>

        <div class="pt-5">
          <div class="flex justify-start">
            <VButton type="secondary"> 保存</VButton>
          </div>
        </div>
      </div>
    </VCard>
  </div>
</template>
