<script lang="ts" setup>
import type { Theme, ThemeCapabilities } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconMore,
  Toast,
  VAvatar,
  VButton,
  VDescription,
  VDescriptionItem,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
  VLoading,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import type { Ref } from "vue";
import { computed, inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import ThemeTemplatesModal from "./components/detail/ThemeTemplatesModal.vue";
import ThemeUiCapabilitiesModal from "./components/detail/ThemeUiCapabilitiesModal.vue";
import { useThemeConfigFile, useThemeLifeCycle } from "./composables/use-theme";
import { useThemeUiStatus } from "./composables/use-theme-ui-status";
import {
  getPageLayoutDescriptionKey,
  getPageLayoutDiagnostic,
  getPageLayoutDotState,
  getPageLayoutLabelKey,
} from "./utils/page-layout";
import {
  getUsageTypeLabelKey,
  summarizeTemplates,
} from "./utils/template-capabilities";
import { hasDeclaredCapabilities } from "./utils/ui-capabilities";

const { t } = useI18n();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const themesModal = inject<Ref<boolean>>("themesModal");

const {
  isActivated,
  isActivationKnown,
  getFailedMessage,
  handleResetSettingConfig,
} = useThemeLifeCycle(selectedTheme);

const {
  data: capabilities,
  isInitialLoading: capabilitiesLoading,
  isError: capabilitiesError,
  refetch: refetchCapabilities,
} = useQuery<ThemeCapabilities>({
  queryKey: [
    "theme-capabilities",
    computed(() => selectedTheme.value?.metadata.name),
  ],
  queryFn: async () => {
    const { data } = await consoleApiClient.theme.theme.fetchThemeCapabilities({
      name: selectedTheme.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(
    () => !!selectedTheme.value && utils.permission.has(["system:themes:view"])
  ),
});

const pageLayout = computed(() => capabilities.value?.pageLayout);
const pageLayoutLabelKey = computed(() =>
  getPageLayoutLabelKey(pageLayout.value?.state)
);
const pageLayoutDescriptionKey = computed(() =>
  getPageLayoutDescriptionKey(pageLayout.value?.state)
);
const pageLayoutDotState = computed(() =>
  getPageLayoutDotState(pageLayout.value?.state)
);
const pageLayoutDiagnostic = computed(() =>
  getPageLayoutDiagnostic(pageLayout.value)
);

const templateSummaries = computed(() =>
  summarizeTemplates(capabilities.value?.templates || []).filter(
    (summary) => summary.total > 0
  )
);

const templatesModalVisible = ref(false);
const uiCapabilitiesModalVisible = ref(false);

const uiResources = computed(() => capabilities.value?.ui);
const { loadState, versionMismatch, diagnostics, moduleSummary } =
  useThemeUiStatus(selectedTheme, uiResources);

const uiLoadStateLabelKey = computed(() => {
  switch (loadState.value) {
    case "not-loaded":
      return "core.theme.ui.load_state.not_loaded";
    case "pending":
      return "core.theme.ui.load_state.pending";
    case "failed":
      return "core.theme.ui.load_state.failed";
    case "registered":
      return "core.theme.ui.load_state.registered";
    default:
      return undefined;
  }
});

const uiLoadStateDotState = computed(() => {
  switch (loadState.value) {
    case "registered":
      return "success" as const;
    case "failed":
      return "error" as const;
    case "pending":
      return "default" as const;
    default:
      return "default" as const;
  }
});

const uiSummaryEntries = computed(() => {
  const summary = moduleSummary.value;
  if (!summary) {
    return [];
  }
  return [
    {
      labelKey: "core.theme.ui.modal.groups.console_routes",
      count: summary.consoleRoutes.length,
    },
    {
      labelKey: "core.theme.ui.modal.groups.uc_routes",
      count: summary.ucRoutes.length,
    },
    {
      labelKey: "core.theme.ui.modal.groups.components",
      count: summary.components.length,
    },
    {
      labelKey: "core.theme.ui.modal.groups.extension_points",
      count: summary.extensionPoints.length,
    },
    {
      labelKey: "core.theme.ui.modal.groups.formkit_inputs",
      count: summary.formkitInputs.length,
    },
  ].filter((entry) => entry.count > 0);
});

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

      await consoleApiClient.theme.theme.invalidateCache({
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

        await consoleApiClient.theme.theme.reload({
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

const { handleExportThemeConfiguration, openSelectImportFileDialog } =
  useThemeConfigFile(selectedTheme);
</script>

<template>
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
              <VTag v-if="isActivationKnown">
                {{
                  isActivated
                    ? t("core.common.status.activated")
                    : t("core.common.status.not_activated")
                }}
              </VTag>
              <VTag
                v-if="selectedTheme?.status?.inDevelopment"
                v-tooltip="$t('core.theme.detail.in_development_tooltip')"
              >
                {{ $t("core.theme.detail.in_development") }}
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
            <VDropdownItem @click="openSelectImportFileDialog()">
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
        <VDescriptionItem label="ID" :content="selectedTheme?.metadata.name" />
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
              selectedTheme?.spec.license && selectedTheme?.spec.license.length
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
    <div class="border-t border-gray-200">
      <h3
        class="border-b border-gray-100 bg-gray-50 px-4 py-3 text-sm font-medium text-gray-900 sm:px-6"
      >
        {{ $t("core.theme.detail.sections.capabilities") }}
      </h3>
      <VLoading v-if="capabilitiesLoading" />
      <div
        v-else-if="capabilitiesError"
        class="flex items-center gap-3 px-4 py-5 sm:px-6"
      >
        <span class="text-sm text-gray-500">
          {{ $t("core.common.status.loading_error") }}
        </span>
        <VButton size="sm" @click="refetchCapabilities()">
          {{ $t("core.common.buttons.retry") }}
        </VButton>
      </div>
      <VDescription v-else-if="capabilities">
        <VDescriptionItem :label="$t('core.theme.detail.fields.page_layout')">
          <div v-if="pageLayout?.state" class="space-y-1">
            <div class="flex items-center gap-2">
              <VStatusDot :state="pageLayoutDotState" />
              <span>{{ $t(pageLayoutLabelKey) }}</span>
            </div>
            <p class="text-xs text-gray-500">
              {{ $t(pageLayoutDescriptionKey) }}
            </p>
            <p
              v-if="pageLayoutDiagnostic"
              class="break-all text-xs text-gray-500"
            >
              {{ pageLayoutDiagnostic }}
            </p>
          </div>
          <span v-else>
            {{ $t("core.common.text.none") }}
          </span>
        </VDescriptionItem>
        <VDescriptionItem :label="$t('core.theme.detail.fields.templates')">
          <div class="space-y-2">
            <p class="flex flex-wrap items-center gap-x-4 gap-y-1">
              <span v-for="summary in templateSummaries" :key="summary.type">
                {{ $t(getUsageTypeLabelKey(summary.type)) }}
                <span class="font-medium">{{ summary.total }}</span>
                <span
                  v-if="summary.unavailable > 0"
                  v-tooltip="
                    $t('core.theme.templates.unavailable_count', {
                      count: summary.unavailable,
                    })
                  "
                  class="text-red-600"
                >
                  ({{ summary.unavailable }})
                </span>
              </span>
            </p>
            <p v-if="!capabilities.complete" class="text-xs text-yellow-600">
              {{ $t("core.theme.templates.incomplete_notice") }}
            </p>
            <div>
              <VButton size="sm" @click="templatesModalVisible = true">
                {{ $t("core.theme.templates.view_all") }}
              </VButton>
            </div>
          </div>
        </VDescriptionItem>
        <VDescriptionItem :label="$t('core.theme.detail.fields.ui')">
          <div class="space-y-2">
            <div class="flex flex-wrap items-center gap-2">
              <VTag v-if="uiResources?.kind === 'esm'">ESM</VTag>
              <VTag v-else-if="uiResources?.kind === 'legacy'">Legacy</VTag>
              <template v-else-if="uiResources?.kind === 'invalid'">
                <VStatusDot state="error" />
                <span>{{ $t("core.theme.ui.kind.invalid") }}</span>
              </template>
              <span v-else>{{ $t("core.theme.ui.kind.none") }}</span>
              <span
                v-if="uiResources?.reason"
                class="break-all text-xs text-gray-500"
              >
                {{ uiResources.reason }}
              </span>
            </div>
            <template v-if="uiResources?.kind !== 'none'">
              <p
                v-if="uiResources?.entry"
                class="break-all text-xs text-gray-500"
              >
                {{ uiResources.entry }}
              </p>
              <p
                v-if="uiResources?.style"
                class="break-all text-xs text-gray-500"
              >
                {{ uiResources.style }}
              </p>
              <div v-if="uiLoadStateLabelKey" class="flex items-center gap-2">
                <VStatusDot
                  :state="uiLoadStateDotState"
                  :animate="loadState === 'pending'"
                />
                <span>{{ $t(uiLoadStateLabelKey) }}</span>
                <span
                  v-if="loadState === 'not-loaded'"
                  class="text-xs text-gray-500"
                >
                  {{ $t("core.theme.ui.load_state.not_loaded_hint") }}
                </span>
              </div>
              <ul v-if="diagnostics.length" class="space-y-1">
                <li
                  v-for="(diagnostic, index) in diagnostics"
                  :key="index"
                  class="break-all text-xs text-red-600"
                >
                  {{ diagnostic.stage }}: {{ diagnostic.message }}
                </li>
              </ul>
              <p v-if="versionMismatch" class="text-xs text-yellow-600">
                {{ $t("core.theme.ui.version_mismatch") }}
              </p>
            </template>
            <template v-if="moduleSummary">
              <p
                v-if="hasDeclaredCapabilities(moduleSummary)"
                class="flex flex-wrap items-center gap-x-4 gap-y-1"
              >
                <span v-for="entry in uiSummaryEntries" :key="entry.labelKey">
                  {{ $t(entry.labelKey) }}
                  <span class="font-medium">{{ entry.count }}</span>
                </span>
              </p>
              <p v-else class="text-xs text-gray-500">
                {{ $t("core.theme.ui.declared.none") }}
              </p>
              <div v-if="hasDeclaredCapabilities(moduleSummary)">
                <VButton size="sm" @click="uiCapabilitiesModalVisible = true">
                  {{ $t("core.theme.ui.declared.view") }}
                </VButton>
              </div>
            </template>
          </div>
        </VDescriptionItem>
      </VDescription>
    </div>
    <ThemeTemplatesModal
      v-if="templatesModalVisible && capabilities"
      :templates="capabilities.templates"
      :complete="capabilities.complete"
      @close="templatesModalVisible = false"
    />
    <ThemeUiCapabilitiesModal
      v-if="uiCapabilitiesModalVisible && moduleSummary"
      :summary="moduleSummary"
      @close="uiCapabilitiesModalVisible = false"
    />
  </div>
</template>
