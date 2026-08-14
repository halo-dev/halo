<script lang="ts" setup>
import { useThemeStore } from "@console/stores/theme";
import type {
  Theme,
  ThemeV1alpha1ConsoleApiListThemesRequest,
} from "@halo-dev/api-client";
import {
  consoleApiClient,
  coreApiClient,
  paginate,
} from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconRefreshLine,
  Toast,
  VButton,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useFuse } from "@vueuse/integrations/useFuse";
import { computed, inject, ref, shallowRef, watch, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import ThemePreviewModal from "../preview/ThemePreviewModal.vue";
import ThemeListItem from "../ThemeListItem.vue";

const themeStore = useThemeStore();
const { t } = useI18n();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const activeTabId = inject<Ref<string>>("activeTabId", ref(""));
const keyword = shallowRef<string>("");

function handleSelectTheme(theme: Theme) {
  selectedTheme.value = theme;
}

const {
  data: themes,
  isLoading,
  isFetching,
  refetch,
} = useQuery<Theme[]>({
  queryKey: ["installed-themes"],
  queryFn: async () => {
    const themes = await paginate<
      ThemeV1alpha1ConsoleApiListThemesRequest,
      Theme
    >((params) => consoleApiClient.theme.theme.listThemes(params), {
      uninstalled: false,
      size: 1000,
    });

    return themes.sort((a, b) => {
      const activatedThemeName = themeStore.activatedTheme?.metadata.name;
      if (a.metadata.name === activatedThemeName) {
        return -1;
      }
      if (b.metadata.name === activatedThemeName) {
        return 1;
      }
      return 0;
    });
  },
  refetchInterval(data) {
    const hasDeletingTheme = data?.some(
      (theme) => !!theme.metadata.deletionTimestamp
    );

    return hasDeletingTheme ? 1000 : false;
  },
});

const { results } = useFuse(
  keyword,
  computed(() => themes.value || []),
  {
    fuseOptions: {
      keys: ["metadata.name", "spec.displayName", "spec.description"],
    },
    matchAllWhenSearchEmpty: true,
  }
);

// preview
const previewVisible = ref(false);
const selectedPreviewTheme = ref<Theme>();

const handleOpenPreview = (theme: Theme) => {
  selectedPreviewTheme.value = theme;
  previewVisible.value = true;
};

// Selected themes for batch operations (like uninstall)
const selectedThemeNames = ref<string[]>([]);
const selectedThemes = computed(() => {
  return (
    themes.value?.filter((theme) =>
      selectedThemeNames.value.includes(theme.metadata.name)
    ) || []
  );
});

watch(
  () => themes.value,
  () => {
    selectedThemeNames.value.length = 0;
  },
  {
    immediate: true,
  }
);

const deleteThemeExtensions = async (theme: Theme) => {
  const { settingName, configMapName } = theme.spec;

  if (settingName) {
    await coreApiClient.setting.deleteSetting(
      {
        name: settingName,
      },
      {
        mute: true,
      }
    );
  }

  if (configMapName) {
    await coreApiClient.configMap.deleteConfigMap(
      {
        name: configMapName,
      },
      {
        mute: true,
      }
    );
  }
};

const uninstallSelectedThemes = async (
  themesToUninstall: Theme[],
  deleteExtensions: boolean
) => {
  try {
    for (const theme of themesToUninstall) {
      await coreApiClient.theme.theme.deleteTheme(
        {
          name: theme.metadata.name,
        },
        {
          mute: true,
        }
      );

      if (deleteExtensions) {
        await deleteThemeExtensions(theme);
      }
    }

    selectedThemeNames.value.length = 0;
    Toast.success(t("core.common.toast.uninstall_success"));
  } catch (error) {
    Toast.error(t("core.common.toast.operation_failed"));
    console.error("Failed to uninstall themes in batch", error);
  } finally {
    await refetch();
  }
};

const confirmDevelopmentThemesUninstall = (
  themesToUninstall: Theme[],
  deleteExtensions: boolean
) => {
  Dialog.warning({
    title: t(
      "core.theme.operations.uninstall_in_batch.possible_development_title"
    ),
    description: t(
      "core.theme.operations.uninstall_in_batch.possible_development_description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: () =>
      uninstallSelectedThemes(themesToUninstall, deleteExtensions),
  });
};

const handleUninstallInBatch = (deleteExtensions: boolean) => {
  const themesToUninstall = [...selectedThemes.value];

  Dialog.warning({
    title: deleteExtensions
      ? t("core.theme.operations.uninstall_and_delete_config_in_batch.title")
      : t("core.theme.operations.uninstall_in_batch.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: () => {
      if (
        themesToUninstall.some((theme) => theme.status?.inDevelopment === true)
      ) {
        confirmDevelopmentThemesUninstall(themesToUninstall, deleteExtensions);
        return;
      }

      return uninstallSelectedThemes(themesToUninstall, deleteExtensions);
    },
  });
};
</script>

<template>
  <div id="installed-themes-wrapper">
    <div class=":uno: mb-3 block w-full rounded bg-gray-50 px-3 py-2">
      <div
        class=":uno: relative flex flex-col items-start sm:flex-row sm:items-center"
      >
        <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
          <SearchInput
            v-if="selectedThemeNames.length === 0"
            v-model="keyword"
            sync
          />
          <VDropdown v-else>
            <VButton type="danger">{{
              $t("core.common.buttons.uninstall")
            }}</VButton>
            <template #popper>
              <VDropdownItem
                type="danger"
                @click="handleUninstallInBatch(false)"
              >
                {{ $t("core.common.buttons.uninstall") }}
              </VDropdownItem>
              <VDropdownItem
                type="danger"
                @click="handleUninstallInBatch(true)"
              >
                {{
                  $t("core.theme.operations.uninstall_and_delete_config.button")
                }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </div>
        <div class=":uno: mt-4 flex sm:mt-0">
          <button
            v-tooltip="$t('core.common.buttons.refresh')"
            type="button"
            class=":uno: group cursor-pointer rounded p-1 hover:bg-gray-200"
            @click="refetch()"
          >
            <IconRefreshLine
              :class="{ ':uno: animate-spin text-gray-900': isFetching }"
              class=":uno: h-4 w-4 text-gray-600 group-hover:text-gray-900"
            />
          </button>
        </div>
      </div>
    </div>
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!themes?.length" appear name="fade">
      <VEmpty
        :message="$t('core.theme.list_modal.empty.message')"
        :title="$t('core.theme.list_modal.empty.title')"
      >
        <template #actions>
          <VSpace>
            <VButton :loading="isFetching" @click="refetch()">
              {{ $t("core.common.buttons.refresh") }}
            </VButton>
            <VButton
              v-permission="['system:themes:manage']"
              type="secondary"
              @click="activeTabId = 'local-upload'"
            >
              <template #icon>
                <IconAddCircle />
              </template>
              {{ $t("core.theme.common.buttons.install") }}
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <ul class="box-border h-full w-full space-y-3" role="list">
        <li
          v-for="theme in results"
          :key="theme.item.metadata.name"
          class="group relative"
        >
          <ThemeListItem
            :theme="theme.item"
            :is-selected="
              theme.item.metadata.name === selectedTheme?.metadata?.name
            "
            @select="handleSelectTheme"
            @preview="handleOpenPreview(theme.item)"
          />
          <input
            v-model="selectedThemeNames"
            v-permission="['system:themes:manage']"
            class="absolute right-1.5 top-1.5 opacity-0 group-hover:opacity-100"
            :class="{
              '!opacity-100': selectedThemeNames.length > 0,
            }"
            :value="theme.item.metadata.name"
            type="checkbox"
          />
        </li>
      </ul>
    </Transition>
    <ThemePreviewModal
      v-if="previewVisible"
      :theme="selectedPreviewTheme"
      @close="previewVisible = false"
    />
  </div>
</template>
