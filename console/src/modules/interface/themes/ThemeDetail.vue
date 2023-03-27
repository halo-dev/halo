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
  Dialog,
  VAvatar,
  Toast,
  VStatusDot,
  VDropdown,
  VDropdownItem,
  VDropdownDivider,
} from "@halo-dev/components";
import ThemeUploadModal from "./components/ThemeUploadModal.vue";

// types
import type { Ref } from "vue";
import type { Theme } from "@halo-dev/api-client";

import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const upgradeModal = ref(false);

const { isActivated, getFailedMessage, handleResetSettingConfig } =
  useThemeLifeCycle(selectedTheme);

const handleReloadTheme = async () => {
  Dialog.warning({
    title: t("core.theme.operations.reload.title"),
    description: t("core.theme.operations.reload.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        if (!selectedTheme?.value) {
          return;
        }

        await apiClient.theme.reload({
          name: selectedTheme.value.metadata.name as string,
        });

        Toast.success(t("core.theme.operations.reload.toast_success"));

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
                  {{
                    isActivated
                      ? t("core.common.status.activated")
                      : t("core.common.status.not_activated")
                  }}
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
          <VDropdown v-permission="['system:themes:manage']">
            <div
              class="cursor-pointer rounded p-1 transition-all hover:text-blue-600 group-hover:bg-gray-100"
            >
              <IconMore />
            </div>
            <template #popper>
              <VDropdownItem @click="upgradeModal = true">
                {{ $t("core.common.buttons.upgrade") }}
              </VDropdownItem>
              <VDropdownDivider />
              <VDropdownItem type="danger" @click="handleReloadTheme">
                {{ $t("core.theme.operations.reload.button") }}
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleResetSettingConfig">
                {{ $t("core.common.buttons.reset") }}
              </VDropdownItem>
            </template>
          </VDropdown>
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
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.author") }}
            </dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.author.name }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.website") }}
            </dt>
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
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.repo") }}
            </dt>
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
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.version") }}
            </dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.version }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.requires") }}
            </dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.spec.requires }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.storage_location") }}
            </dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
              {{ selectedTheme?.status?.location }}
            </dd>
          </div>
          <!-- TODO: add display required plugins support -->
          <div
            v-if="false"
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">
              {{ $t("core.theme.detail.fields.plugin_requires") }}
            </dt>
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
                        <VTag>{{ $t("core.common.status.installed") }}</VTag>
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
                        <VTag>
                          {{ $t("core.common.status.not_installed") }}
                        </VTag>
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
