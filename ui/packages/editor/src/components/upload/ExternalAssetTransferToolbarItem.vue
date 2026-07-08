<script lang="ts" setup>
import { VButton, VDropdown, vTooltip } from "@halo-dev/components";
import { computed, shallowRef, watch } from "vue";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import type { ExtensionUploadStorage } from "@/extensions/upload";
import { i18n } from "@/locales";
import type { ToolbarItemComponentProps } from "@/types";

const props = defineProps<ToolbarItemComponentProps>();

const dropdownShown = shallowRef(false);

const uploadStorage = computed(() => {
  return (props.editor.storage as { upload?: ExtensionUploadStorage }).upload;
});
const externalAssetState = computed(() => {
  return uploadStorage.value?.externalAssetPrompt;
});
const assetItems = computed(() => {
  return externalAssetState.value?.items.value || [];
});
const externalAssetCount = computed(() => {
  return externalAssetState.value?.count.value || 0;
});
const badgeText = computed(() => {
  if (externalAssetCount.value > 99) {
    return "99+";
  }
  return externalAssetCount.value.toString();
});
const scanning = computed(() => {
  return externalAssetState.value?.scanning.value || false;
});
const transferring = computed(() => {
  return externalAssetState.value?.transferring.value || false;
});
const disabled = computed(() => {
  return !uploadStorage.value?.uploadExternalUrl;
});

watch(dropdownShown, (shown) => {
  if (!shown) {
    return;
  }

  void handleScan();
});

async function handleScan() {
  if (!uploadStorage.value?.uploadExternalUrl) {
    return;
  }

  await uploadStorage.value.scanExternalAssets();
}

async function handleTransfer() {
  if (!uploadStorage.value?.uploadExternalUrl || !externalAssetCount.value) {
    return;
  }

  await uploadStorage.value.transferExternalAssets();
  dropdownShown.value = false;
}
</script>

<template>
  <VDropdown
    v-model:shown="dropdownShown"
    class="inline-flex"
    tabindex="-1"
    :distance="8"
    :triggers="['click']"
    :popper-triggers="['click']"
  >
    <template #default="{ shown }">
      <button
        v-tooltip="
          i18n.global.t('editor.extensions.upload.external_asset_tool.tooltip')
        "
        :class="[
          { 'bg-gray-200/70': shown || externalAssetCount > 0 },
          { 'cursor-not-allowed opacity-70': disabled },
          { 'hover:bg-gray-100': !disabled },
        ]"
        class="relative inline-flex size-8 items-center justify-center rounded-md p-1 transition-colors active:!bg-gray-200"
        :disabled="disabled"
        tabindex="-1"
      >
        <MingcuteLinkLine />
        <span
          v-if="externalAssetCount"
          class="absolute -right-1 -top-1 min-w-4 rounded-full bg-primary px-1 text-[10px] font-medium leading-4 text-white"
        >
          {{ badgeText }}
        </span>
      </button>
    </template>
    <template #popper>
      <div class="w-96 max-w-[calc(100vw-2rem)] p-3">
        <div class="mb-3 flex items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="text-sm font-medium text-gray-900">
              {{
                i18n.global.t(
                  "editor.extensions.upload.external_asset_tool.title"
                )
              }}
            </div>
            <div class="mt-1 text-xs text-gray-500">
              {{
                i18n.global.t(
                  "editor.extensions.upload.external_asset_tool.description",
                  { count: externalAssetCount }
                )
              }}
            </div>
          </div>
          <VButton
            size="xs"
            ghost
            :loading="scanning"
            :disabled="transferring"
            @click="handleScan"
          >
            {{
              i18n.global.t("editor.extensions.upload.external_asset_tool.scan")
            }}
          </VButton>
        </div>

        <div
          v-if="scanning"
          class="rounded-base border border-gray-100 bg-gray-50 px-3 py-6 text-center text-sm text-gray-500"
        >
          {{
            i18n.global.t(
              "editor.extensions.upload.external_asset_tool.scanning"
            )
          }}
        </div>
        <div
          v-else-if="!assetItems.length"
          class="rounded-base border border-gray-100 bg-gray-50 px-3 py-6 text-center text-sm text-gray-500"
        >
          {{
            i18n.global.t("editor.extensions.upload.external_asset_tool.empty")
          }}
        </div>
        <ul v-else class="max-h-64 divide-y divide-gray-100 overflow-y-auto">
          <li
            v-for="item in assetItems"
            :key="item.url"
            class="flex items-center gap-2 py-2 text-sm"
          >
            <span
              class="min-w-0 flex-1 truncate font-mono text-xs text-gray-700"
              :title="item.url"
            >
              {{ item.url }}
            </span>
            <span
              v-if="item.count > 1"
              class="shrink-0 rounded-full bg-gray-100 px-1.5 py-0.5 text-xs text-gray-500"
            >
              x{{ item.count }}
            </span>
          </li>
        </ul>

        <div class="mt-3 flex justify-end gap-2 border-t border-gray-100 pt-3">
          <VButton size="xs" ghost @click="dropdownShown = false">
            {{ i18n.global.t("editor.common.button.cancel") }}
          </VButton>
          <VButton
            size="xs"
            type="secondary"
            :disabled="!externalAssetCount || scanning"
            :loading="transferring"
            @click="handleTransfer"
          >
            {{
              i18n.global.t(
                "editor.extensions.upload.external_asset_tool.confirm"
              )
            }}
          </VButton>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
