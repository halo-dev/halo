<script lang="ts" setup>
// core libs
import { inject, ref } from "vue";
import { RouterLink } from "vue-router";
import { useThemeLifeCycle } from "./composables/use-theme";

// components
import {
  VAlert,
  VSpace,
  VTag,
  IconMore,
  VButton,
  Dialog,
  VAvatar,
  Toast,
  VStatusDot,
} from "@halo-dev/components";
import ThemeUploadModal from "./components/ThemeUploadModal.vue";

// types
import type { Ref } from "vue";
import type { Theme } from "@halo-dev/api-client";

import { apiClient } from "@/utils/api-client";

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const upgradeModal = ref(false);

const { isActivated, getFailedMessage, handleResetSettingConfig } =
  useThemeLifeCycle(selectedTheme);

const handleReloadTheme = async () => {
  Dialog.warning({
    title: "确定要重载主题的所有配置吗？",
    description: "该操作仅会重载主题配置和设置表单定义，不会删除已保存的配置。",
    onConfirm: async () => {
      try {
        if (!selectedTheme?.value) {
          return;
        }

        await apiClient.theme.reload({
          name: selectedTheme.value.metadata.name as string,
        });

        Toast.success("重载配置成功");

        window.location.reload();
      } catch (e) {
        console.error("Failed to reload theme setting", e);
      }
    },
  });
};

const onUpgradeModalClose = () => {
  setTimeout(() => {
    window.location.reload();
  }, 200);
};
</script>

<template>
  <Transition mode="out-in" name="fade">
    <div>
      <div class="bg-white px-4 py-4 sm:px-6">
        <div class="group flex items-center justify-between">
          <div class="flex flex-row items-center gap-3">
            <VAvatar
              :key="selectedTheme?.metadata.name"
              :alt="selectedTheme?.spec.displayName"
              :src="selectedTheme?.spec.logo"
              size="lg"
            />
            <div>
              <h3 class="text-lg font-medium leading-6 text-gray-900">
                {{ selectedTheme?.spec.displayName }}
              </h3>
              <p class="mt-1 flex max-w-2xl items-center gap-2">
                <span class="text-sm text-gray-500">
                  {{ selectedTheme?.spec.version }}
                </span>
                <VTag>
                  {{ isActivated ? "当前启用" : "未启用" }}
                </VTag>
                <VStatusDot
                  v-if="getFailedMessage()"
                  v-tooltip="getFailedMessage()"
                  state="warning"
                  animate
                />
              </p>
            </div>
          </div>
          <FloatingDropdown v-permission="['system:themes:manage']">
            <div
              class="cursor-pointer rounded p-1 transition-all hover:text-blue-600 group-hover:bg-gray-100"
            >
              <IconMore />
            </div>
            <template #popper>
              <div class="w-48 p-2">
                <VSpace class="w-full" direction="column">
                  <VButton
                    v-close-popper
                    block
                    type="secondary"
                    @click="upgradeModal = true"
                  >
                    升级
                  </VButton>
                  <VButton
                    v-close-popper
                    block
                    type="default"
                    @click="handleReloadTheme"
                  >
                    重载主题配置
                  </VButton>
                  <VButton
                    v-close-popper
                    block
                    type="danger"
                    @click="handleResetSettingConfig"
                  >
                    重置
                  </VButton>
                </VSpace>
              </div>
            </template>
          </FloatingDropdown>
        </div>
      </div>
      <div class="border-t border-gray-200">
        <dl class="divide-y divide-gray-100">
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">ID</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.metadata.name }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">作者</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.author.name }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">网站</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              <a
                :href="selectedTheme?.spec.website"
                class="hover:text-gray-600"
                target="_blank"
              >
                {{ selectedTheme?.spec.website }}
              </a>
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">源码仓库</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              <a
                :href="selectedTheme?.spec.repo"
                class="hover:text-gray-600"
                target="_blank"
              >
                {{ selectedTheme?.spec.repo }}
              </a>
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">当前版本</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.version }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.requires }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">存储位置</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.status?.location }}
            </dd>
          </div>
          <!-- TODO: add display required plugins support -->
          <div
            v-if="false"
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
                    class="inline-flex w-96 cursor-pointer flex-col gap-y-3 rounded border p-5 hover:border-primary"
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
                    class="inline-flex w-96 cursor-pointer flex-col gap-y-3 rounded border p-5 hover:border-primary"
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
  </Transition>
  <ThemeUploadModal
    v-model:visible="upgradeModal"
    :upgrade-theme="selectedTheme"
    @close="onUpgradeModalClose"
  />
</template>
