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
  VModal,
  VPageHeader,
  VSpace,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import type { Metadata } from "@halo-dev/api-client";

interface ThemeAuthor {
  name: string;
  website: string;
}

interface ThemeSpec {
  displayName: string;
  author: ThemeAuthor;
  description?: string;
  logo?: string;
  website?: string;
  repo?: string;
  version: string;
  require: string;
}

interface Theme {
  metadata: Metadata;
  spec: ThemeSpec;
  kind: string;
  apiVersion: string;
}

const themes = ref<Theme[]>([]);
const currentTheme = ref<Theme>({} as Theme);
const themesModal = ref(false);
const themeActiveId = ref("detail");

const handleChangeTheme = (theme: Theme) => {
  currentTheme.value = theme;
  themesModal.value = false;
};

const handleFetchThemes = async () => {
  themes.value = await new Promise((resolve) => {
    resolve([
      {
        apiVersion: "theme.halo.run/v1alpha1",
        kind: "Theme",
        metadata: {
          name: "default",
        },
        spec: {
          displayName: "Default",
          author: {
            name: "halo-dev",
            website: "https://halo.run",
          },
          description: "Halo 2.0 的默认主题",
          logo: "https://halo.run/logo",
          website: "https://github.com/halo-sigs/theme-default.git",
          repo: "https://github.com/halo-sigs/theme-default.git",
          version: "1.0.0",
          require: "2.0.0",
        },
      },
      {
        apiVersion: "theme.halo.run/v1alpha1",
        kind: "Theme",
        metadata: {
          name: "gtvg",
        },
        spec: {
          displayName: "GTVG",
          author: {
            name: "guqing",
            website: "https://guqing.xyz",
          },
          description: "测试主题",
          logo: "https://guqing.xyz/logo.png",
          website: "https://github.com/guqing/halo-theme-test.git",
          repo: "https://github.com/guqing/halo-theme-test.git",
          version: "1.0.0",
          require: "2.0.0",
        },
      },
    ]);
  });
  currentTheme.value = themes.value[0];
};

onMounted(handleFetchThemes);
</script>

<template>
  <VModal
    v-model:visible="themesModal"
    :body-class="['!p-0']"
    :width="888"
    title="已安装的主题"
  >
    <ul class="flex flex-col divide-y divide-gray-100" role="list">
      <li
        v-for="(theme, index) in themes"
        :key="index"
        :class="{
          'bg-gray-50': theme.metadata.name === currentTheme.metadata?.name,
        }"
        class="relative cursor-pointer py-4 transition-all hover:bg-gray-100"
        @click="handleChangeTheme(theme)"
      >
        <div class="flex items-center">
          <div
            v-show="theme.metadata.name === currentTheme.metadata?.name"
            class="absolute inset-y-0 left-0 w-0.5 bg-primary"
          ></div>
          <div class="w-40 px-4">
            <div
              class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
            >
              <img
                :src="theme.spec.logo"
                alt=""
                class="pointer-events-none object-cover group-hover:opacity-75"
              />
            </div>
          </div>
          <div class="flex-1">
            <VSpace align="start" direction="column" spacing="xs">
              <div class="flex items-center gap-2">
                <span class="text-lg font-medium text-gray-900">
                  {{ theme.spec.displayName }}
                </span>
                <VTag>当前启用</VTag>
              </div>
              <div>
                <span class="text-sm text-gray-400">
                  {{ theme.spec.version }}
                </span>
              </div>
            </VSpace>
          </div>
          <div class="px-4">
            <VSpace spacing="lg">
              <div>
                <span class="text-sm text-gray-400 hover:text-blue-600">
                  {{ theme.spec.author.name }}
                </span>
              </div>
              <div v-if="theme.spec.website">
                <a
                  :href="theme.spec.website"
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
      <VButton @click="themesModal = false">关闭</VButton>
    </template>
  </VModal>
  <VPageHeader :title="currentTheme.spec?.displayName">
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
        <VButton size="sm" type="primary"> 启用</VButton>
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
            <div v-if="currentTheme.spec?.logo">
              <div
                class="h-12 w-12 overflow-hidden rounded border bg-white hover:shadow-sm"
              >
                <img
                  :alt="currentTheme.spec?.displayName"
                  :src="currentTheme.spec?.logo"
                  class="h-full w-full"
                />
              </div>
            </div>
            <div>
              <h3 class="text-lg font-medium leading-6 text-gray-900">
                {{ currentTheme.spec?.displayName }}
              </h3>
              <p class="mt-1 flex max-w-2xl items-center gap-2">
                <span class="text-sm text-gray-500">
                  {{ currentTheme.spec?.version }}
                </span>
                <VTag> 当前启用</VTag>
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
                {{ currentTheme.metadata?.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">作者</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.spec?.author?.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">网站</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="currentTheme.spec?.website" target="_blank">
                  {{ currentTheme.spec?.website }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">源码仓库</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                <a :href="currentTheme.spec?.website" target="_blank">
                  {{ currentTheme.spec?.website }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">当前版本</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.spec?.version }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
                {{ currentTheme.spec?.require }}
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

      <div v-if="themeActiveId === 'settings'" class="p-4 sm:px-6">
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
