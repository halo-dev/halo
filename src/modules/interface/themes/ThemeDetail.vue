<script lang="ts" setup>
import {
  IconArrowRight,
  IconExchange,
  IconEye,
  IconGitHub,
  IconPalette,
  VAlert,
  VButton,
  VCard,
  VInput,
  VModal,
  VPageHeader,
  VSpace,
  VTabbar,
  VTag,
  VTextarea,
} from "@halo-dev/components";
import { ref } from "vue";
import { RouterLink } from "vue-router";
import { themes } from "@/modules/interface/themes/themes-mock";

const currentTheme = ref(themes[0]);
const changeTheme = ref(false);
const themeActiveId = ref("detail");

// eslint-disable-next-line
const handleChangeTheme = (theme: any) => {
  currentTheme.value = theme;
  changeTheme.value = false;
};
</script>

<template>
  <VModal
    v-model:visible="changeTheme"
    :body-class="['!p-0']"
    :width="888"
    title="已安装的主题"
  >
    <ul class="flex flex-col divide-y divide-gray-100" role="list">
      <li
        v-for="(theme, index) in themes"
        :key="index"
        :class="{ 'bg-gray-50': theme.activated }"
        class="relative cursor-pointer py-4 transition-all hover:bg-gray-100"
        @click="handleChangeTheme(theme)"
      >
        <div class="flex items-center">
          <div
            v-show="theme.activated"
            class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
          ></div>
          <div class="w-40 px-4">
            <div
              class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
            >
              <img
                :src="theme.screenshots"
                alt=""
                class="pointer-events-none object-cover group-hover:opacity-75"
              />
            </div>
          </div>
          <div class="flex-1">
            <VSpace align="start" direction="column" spacing="xs">
              <div class="flex items-center gap-2">
                <span class="text-lg font-medium text-gray-900">
                  {{ theme.name }}
                </span>
                <VTag v-if="theme.activated">当前启用</VTag>
              </div>
              <div>
                <span class="text-sm text-gray-400">{{ theme.version }}</span>
              </div>
            </VSpace>
          </div>
          <div class="px-4">
            <VSpace spacing="lg">
              <div>
                <span class="text-sm text-gray-400 hover:text-blue-600">
                  {{ theme.author.name }}
                </span>
              </div>
              <div v-if="theme.repo">
                <a
                  :href="theme.repo"
                  class="text-gray-900 hover:text-blue-600"
                  target="_blank"
                >
                  <IconGitHub />
                </a>
              </div>
              <div>
                <IconArrowRight class="text-gray-900" />
              </div>
            </VSpace>
          </div>
        </div>
      </li>
    </ul>
    <template #footer>
      <VButton @click="changeTheme = false">关闭</VButton>
    </template>
  </VModal>
  <VPageHeader :title="currentTheme.name">
    <template #icon>
      <IconPalette class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" type="default" @click="changeTheme = true">
          <template #icon>
            <IconExchange class="h-full w-full" />
          </template>
          切换主题
        </VButton>
        <VButton v-if="!currentTheme.activated" size="sm" type="primary">
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
          v-model:active-id="themeActiveId"
          :items="[
            { id: 'detail', label: '详情' },
            { id: 'settings', label: '基础设置' },
          ]"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>

      <div v-if="themeActiveId === 'detail'">
        <div class="px-4 py-4 sm:px-6">
          <div class="flex flex-row gap-3">
            <div v-if="currentTheme.logo">
              <div
                class="h-12 w-12 overflow-hidden rounded border bg-white hover:shadow-sm"
              >
                <img
                  :alt="currentTheme.name"
                  :src="currentTheme.logo"
                  class="h-full w-full"
                />
              </div>
            </div>
            <div>
              <h3 class="text-lg font-medium leading-6 text-gray-900">
                {{ currentTheme.name }}
              </h3>
              <p class="mt-1 flex max-w-2xl items-center gap-2">
                <span class="text-sm text-gray-500">
                  {{ currentTheme.version }}
                </span>
                <VTag>
                  {{ currentTheme.activated ? "当前启用" : "未启用" }}
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
                {{ currentTheme.id }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">作者</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.author.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">网站</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="currentTheme.website" target="_blank">
                  {{ currentTheme.website }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">源码仓库</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="currentTheme.repo" target="_blank">
                  {{ currentTheme.repo }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">当前版本</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.version }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.require }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">存储位置</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.themePath }}
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
                      class="inline-flex w-96 cursor-pointer flex-row flex-col gap-y-3 rounded border p-5 hover:border-themeable-primary"
                    >
                      <RouterLink
                        :to="{
                          name: 'PluginDetail',
                          params: { pluginName: 'PluginLinks' },
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
                      class="inline-flex w-96 cursor-pointer flex-row flex-col gap-y-3 rounded border p-5 hover:border-themeable-primary"
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

      <div v-if="themeActiveId === 'settings'">
        <form class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
          <div class="space-y-6 space-y-5 divide-y divide-gray-100">
            <div
              class="px-4 sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5"
            >
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                侧边栏宽度
              </label>
              <div class="mt-1 sm:col-span-3 sm:mt-0">
                <div class="flex max-w-lg shadow-sm">
                  <VInput />
                </div>
              </div>
            </div>
            <div
              class="px-4 sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5"
            >
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                侧边栏背景图
              </label>
              <div class="mt-1 sm:col-span-3 sm:mt-0">
                <div class="flex max-w-lg shadow-sm">
                  <VInput />
                </div>
              </div>
            </div>

            <div
              class="px-4 sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5"
            >
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                右上角图标
              </label>
              <div class="mt-1 sm:col-span-3 sm:mt-0">
                <div class="flex max-w-lg shadow-sm">
                  <VInput />
                </div>
              </div>
            </div>
            <div
              class="px-4 sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5"
            >
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                文章代码高亮语言
              </label>
              <div class="mt-1 sm:col-span-3 sm:mt-0">
                <div class="flex max-w-lg shadow-sm">
                  <VTextarea modelValue="Halo" />
                </div>
              </div>
            </div>
          </div>

          <div class="pt-5">
            <div class="flex justify-start p-4">
              <VButton type="secondary"> 保存</VButton>
            </div>
          </div>
        </form>
      </div>
    </VCard>
  </div>
</template>
