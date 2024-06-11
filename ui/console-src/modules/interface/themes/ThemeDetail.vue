<script lang="ts" setup>
// types
import type { Ref } from "vue";
// core libs
import { inject, ref } from "vue";
import { useThemeLifeCycle } from "./composables/use-theme";

// components
import {
  Dialog,
  IconMore,
  Toast,
  VAvatar,
  VDescription,
  VDescriptionItem,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import type { Theme } from "@halo-dev/api-client";

import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { useFileDialog } from "@vueuse/core";

const { t } = useI18n();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const themesModal = inject<Ref<boolean>>("themesModal");

const { isActivated, getFailedMessage, handleResetSettingConfig } =
  useThemeLifeCycle(selectedTheme);

async function handleClearCache() {
  Dialog.warning({
    title: t("core.theme.operations.clear_templates_cache.title"),
    description: t("core.theme.operations.clear_templates_cache.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      if (!selectedTheme.value) {
        console.error("No selected or activated theme");
        return;
      }

      await apiClient.theme.invalidateCache({
        name: selectedTheme.value?.metadata.name,
      });

      Toast.success(t("core.common.toast.operation_success"));
    },
  });
}

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

interface ExportData {
  themeName: string;
  version: string;
  settingName: string;
  configMapName: string;
  configs: { [key: string]: string };
}

const handleExportThemeConfiguration = async () => {
  if (!selectedTheme.value) {
    console.error("No selected or activated theme");
    return;
  }

  const { data } = await apiClient.theme.fetchThemeConfig({
    name: selectedTheme?.value?.metadata.name as string,
  });
  if (!data) {
    console.error("Failed to fetch theme config");
    return;
  }

  const themeName = selectedTheme.value.metadata.name;
  const exportData = {
    themeName: themeName,
    version: selectedTheme.value.spec.version,
    settingName: selectedTheme.value.spec.settingName,
    configMapName: selectedTheme.value.spec.configMapName,
    configs: data.data,
  } as ExportData;
  const exportStr = JSON.stringify(exportData);
  const blob = new Blob([exportStr], { type: "application/json" });
  const temporaryExportUrl = URL.createObjectURL(blob);
  const temporaryLinkTag = document.createElement("a");

  temporaryLinkTag.href = temporaryExportUrl;
  temporaryLinkTag.download = `export-${themeName}-config-${Date.now().toString()}.json`;

  document.body.appendChild(temporaryLinkTag);
  temporaryLinkTag.click();

  document.body.removeChild(temporaryLinkTag);
  URL.revokeObjectURL(temporaryExportUrl);
};

const {
  open: openSelectImportFileDialog,
  onChange: handleImportThemeConfiguration,
} = useFileDialog({
  accept: "application/json",
  multiple: false,
  directory: false,
  reset: true,
});

handleImportThemeConfiguration(async (files) => {
  if (files === null || files.length === 0) {
    return;
  }
  const configText = await files[0].text();
  const configJson = JSON.parse(configText || "{}");
  if (!configJson.configs) {
    return;
  }
  if (!configJson.themeName || !configJson.version) {
    Toast.error(t("core.theme.operations.import_configuration.invalid_format"));
    return;
  }
  if (!selectedTheme.value) {
    console.error("No selected or activated theme");
    return;
  }
  if (configJson.themeName !== selectedTheme.value.metadata.name) {
    Toast.error(
      t("core.theme.operations.import_configuration.mismatched_theme")
    );
    return;
  }

  if (configJson.version !== selectedTheme.value.spec.version) {
    Dialog.warning({
      title: t(
        "core.theme.operations.import_configuration.version_mismatch.title"
      ),
      description: t(
        "core.theme.operations.import_configuration.version_mismatch.description"
      ),
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: () => {
        handleSaveConfigMap(configJson.configs);
      },
      onCancel() {
        return;
      },
    });
    return;
  }
  handleSaveConfigMap(configJson.configs);
});

const handleSaveConfigMap = async (importData: Record<string, string>) => {
  if (!selectedTheme.value) {
    return;
  }
  const { data } = await apiClient.theme.fetchThemeConfig({
    name: selectedTheme.value.metadata.name as string,
  });
  if (!data || !data.data) {
    return;
  }
  const combinedConfigData = combinedConfigMap(data.data, importData);
  await apiClient.theme.updateThemeConfig({
    name: selectedTheme.value.metadata.name,
    configMap: {
      ...data,
      data: combinedConfigData,
    },
  });
  Toast.success(t("core.common.toast.save_success"));
};

/**
 * combined benchmark configuration and import configuration
 *
 * benchmark: { a: "{\"a\": 1}", b: "{\"b\": 2}" }
 * expand: { a: "{\"c\": 3}", b: "{\"d\": 4}" }
 * => { a: "{\"a\": 1, \"c\": 3}", b: "{\"b\": 2, \"d\": 4}" }
 *
 * benchmark: { a: "{\"a\": 1}", b: "{\"b\": 2}", d: "{\"d\": 4}"
 * expand: { a: "{\"a\": 2}", b: "{\"b\": 3, \"d\": 4}", c: "{\"c\": 5}" }
 * => { a: "{\"a\": 2}", b: "{\"b\": 3, \"d\": 4}", d: "{\"d\": 4}" }
 *
 */
const combinedConfigMap = (
  benchmarkConfigMap: { [key: string]: string },
  importConfigMap: { [key: string]: string }
): { [key: string]: string } => {
  const result = benchmarkConfigMap;

  for (const key in result) {
    const benchmarkValueJson = JSON.parse(benchmarkConfigMap[key] || "{}");
    const expandValueJson = JSON.parse(importConfigMap[key] || "{}");
    const combinedValue = {
      ...benchmarkValueJson,
      ...expandValueJson,
    };
    result[key] = JSON.stringify(combinedValue);
  }
  return result;
};
</script>

<template>
  <Transition mode="out-in" name="fade">
    <div class="overflow-hidden rounded-b-base">
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
              <VDropdownItem @click="themesModal = true">
                {{ $t("core.common.buttons.upgrade") }}
              </VDropdownItem>
              <VDropdownItem @click="handleExportThemeConfiguration">
                {{ $t("core.theme.operations.export_configuration.button") }}
              </VDropdownItem>
              <VDropdownItem @click="openSelectImportFileDialog">
                {{ $t("core.theme.operations.import_configuration.button") }}
              </VDropdownItem>
              <VDropdownDivider />
              <VDropdownItem type="danger" @click="handleReloadTheme">
                {{ $t("core.theme.operations.reload.button") }}
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleClearCache">
                {{ $t("core.theme.operations.clear_templates_cache.button") }}
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleResetSettingConfig">
                {{ $t("core.common.buttons.reset") }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </div>
      </div>
      <div class="border-t border-gray-200">
        <VDescription>
          <VDescriptionItem
            label="ID"
            :content="selectedTheme?.metadata.name"
          />
          <VDescriptionItem
            :label="$t('core.theme.detail.fields.description')"
            :content="
              selectedTheme?.spec.description || $t('core.common.text.none')
            "
          ></VDescriptionItem>
          <VDescriptionItem :label="$t('core.theme.detail.fields.author')">
            <a
              v-if="selectedTheme?.spec.author"
              :href="selectedTheme.spec.author.website || '#'"
              class="hover:text-gray-600"
              target="_blank"
            >
              {{ selectedTheme.spec.author.name }}
            </a>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.theme.detail.fields.version')"
            :content="selectedTheme?.spec.version"
          />
          <VDescriptionItem
            :label="$t('core.theme.detail.fields.requires')"
            :content="selectedTheme?.spec.requires"
          />
          <VDescriptionItem :label="$t('core.theme.detail.fields.homepage')">
            <a
              v-if="selectedTheme?.spec.homepage"
              :href="selectedTheme.spec.homepage"
              class="hover:text-gray-600"
              target="_blank"
            >
              {{ selectedTheme.spec.homepage }}
            </a>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem :label="$t('core.theme.detail.fields.repo')">
            <a
              v-if="selectedTheme?.spec.repo"
              :href="selectedTheme.spec.repo"
              class="hover:text-gray-600"
              target="_blank"
            >
              {{ selectedTheme.spec.repo }}
            </a>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem :label="$t('core.theme.detail.fields.issues')">
            <a
              v-if="selectedTheme?.spec.issues"
              :href="selectedTheme.spec.issues"
              class="hover:text-gray-600"
              target="_blank"
            >
              {{ selectedTheme.spec.issues }}
            </a>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem :label="$t('core.theme.detail.fields.license')">
            <ul
              v-if="
                selectedTheme?.spec.license &&
                selectedTheme?.spec.license.length
              "
              class="list-inside"
              :class="{ 'list-disc': selectedTheme?.spec.license.length > 1 }"
            >
              <li
                v-for="(license, index) in selectedTheme.spec.license"
                :key="index"
              >
                <a v-if="license.url" :href="license.url" target="_blank">
                  {{ license.name }}
                </a>
                <span v-else>
                  {{ license.name }}
                </span>
              </li>
            </ul>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.theme.detail.fields.storage_location')"
            :content="selectedTheme?.status?.location"
          />
        </VDescription>
      </div>
    </div>
  </Transition>
</template>
