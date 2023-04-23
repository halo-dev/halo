<script lang="ts" setup>
import {
  IconAddCircle,
  IconGitHub,
  VButton,
  VEmpty,
  VModal,
  VSpace,
  VEntity,
  VEntityField,
  VTabItem,
  VTabs,
  VLoading,
  Toast,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import ThemePreviewModal from "./preview/ThemePreviewModal.vue";
import ThemeUploadModal from "./ThemeUploadModal.vue";
import ThemeListItem from "./components/ThemeListItem.vue";
import { computed, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { useQuery } from "@tanstack/vue-query";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    visible: boolean;
    selectedTheme?: Theme;
  }>(),
  {
    visible: false,
    selectedTheme: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "update:selectedTheme", theme?: Theme): void;
  (event: "select", theme: Theme | null): void;
}>();

const activeTab = ref("installed");
const themeUploadVisible = ref(false);
const creating = ref(false);

const modalTitle = computed(() => {
  return activeTab.value === "installed"
    ? t("core.theme.list_modal.titles.installed_themes")
    : t("core.theme.list_modal.titles.not_installed_themes");
});

const {
  data: themes,
  isLoading,
  isFetching,
  refetch,
} = useQuery<Theme[]>({
  queryKey: ["themes", activeTab],
  queryFn: async () => {
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: activeTab.value !== "installed",
    });
    return data.items;
  },
  refetchInterval(data) {
    if (activeTab.value !== "installed") {
      return false;
    }

    const deletingThemes = data?.filter(
      (theme) => !!theme.metadata.deletionTimestamp
    );

    return deletingThemes?.length ? 3000 : false;
  },
  enabled: computed(() => props.visible),
});

const handleCreateTheme = async (theme: Theme) => {
  try {
    creating.value = true;

    const { data } =
      await apiClient.extension.theme.createthemeHaloRunV1alpha1Theme({
        theme,
      });

    // create theme settings
    apiClient.theme.reload({ name: data.metadata.name });

    activeTab.value = "installed";

    Toast.success(t("core.common.toast.install_success"));
  } catch (error) {
    console.error("Failed to create theme", error);
  } finally {
    creating.value = false;
    refetch();
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleSelectTheme = (theme: Theme) => {
  emit("update:selectedTheme", theme);
  emit("select", theme);
  onVisibleChange(false);
};

defineExpose({
  handleFetchThemes: refetch,
});

// preview
const previewVisible = ref(false);
const selectedPreviewTheme = ref<Theme>();

const handleOpenPreview = (theme: Theme) => {
  selectedPreviewTheme.value = theme;
  previewVisible.value = true;
};

// upgrade
const themeToUpgrade = ref<Theme>();

const handleOpenUpgradeModal = (theme: Theme) => {
  themeToUpgrade.value = theme;
  themeUploadVisible.value = true;
};

const handleOpenInstallModal = () => {
  themeToUpgrade.value = undefined;
  themeUploadVisible.value = true;
};
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :width="888"
    height="calc(100vh - 20px)"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <VTabs
      v-model:active-id="activeTab"
      type="outline"
      class="mx-[16px] my-[12px]"
    >
      <VTabItem
        id="installed"
        :label="$t('core.theme.list_modal.tabs.installed')"
        class="-mx-[16px]"
      >
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
                  type="primary"
                  @click="handleOpenInstallModal()"
                >
                  <template #icon>
                    <IconAddCircle class="h-full w-full" />
                  </template>
                  {{ $t("core.theme.common.buttons.install") }}
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li
              v-for="(theme, index) in themes"
              :key="index"
              @click="handleSelectTheme(theme)"
            >
              <ThemeListItem
                :theme="theme"
                :is-selected="
                  theme.metadata.name === selectedTheme?.metadata?.name
                "
                @reload="refetch"
                @preview="handleOpenPreview(theme)"
                @upgrade="handleOpenUpgradeModal(theme)"
              />
            </li>
          </ul>
        </Transition>
      </VTabItem>
      <VTabItem
        id="uninstalled"
        :label="$t('core.theme.list_modal.tabs.not_installed')"
        class="-mx-[16px]"
      >
        <VLoading v-if="isLoading" />
        <Transition v-else-if="!themes?.length" appear name="fade">
          <VEmpty
            :title="$t('core.theme.list_modal.not_installed_empty.title')"
          >
            <template #actions>
              <VSpace>
                <VButton :loading="isFetching" @click="refetch">
                  {{ $t("core.common.buttons.refresh") }}
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li v-for="(theme, index) in themes" :key="index">
              <VEntity>
                <template #start>
                  <VEntityField>
                    <template #description>
                      <div class="w-32">
                        <div
                          class="group aspect-h-3 aspect-w-4 block w-full overflow-hidden rounded border bg-gray-100"
                        >
                          <LazyImage
                            :key="theme.metadata.name"
                            :src="theme.spec.logo"
                            :alt="theme.spec.displayName"
                            classes="pointer-events-none object-cover group-hover:opacity-75"
                          >
                            <template #loading>
                              <div
                                class="flex h-full items-center justify-center object-cover"
                              >
                                <span class="text-xs text-gray-400">
                                  {{ $t("core.common.status.loading") }}...
                                </span>
                              </div>
                            </template>
                            <template #error>
                              <div
                                class="flex h-full items-center justify-center object-cover"
                              >
                                <span class="text-xs text-red-400">
                                  {{ $t("core.common.status.loading_error") }}
                                </span>
                              </div>
                            </template>
                          </LazyImage>
                        </div>
                      </div>
                    </template>
                  </VEntityField>
                  <VEntityField
                    :title="theme.spec.displayName"
                    :description="theme.spec.version"
                  >
                  </VEntityField>
                </template>
                <template #end>
                  <VEntityField>
                    <template #description>
                      <a
                        class="text-sm text-gray-400 hover:text-blue-600"
                        :href="theme.spec.author.website"
                        target="_blank"
                        @click.stop
                      >
                        {{ theme.spec.author.name }}
                      </a>
                    </template>
                  </VEntityField>
                  <VEntityField>
                    <template #description>
                      <a
                        :href="theme.spec.repo"
                        class="text-gray-900 hover:text-blue-600"
                        target="_blank"
                      >
                        <IconGitHub />
                      </a>
                    </template>
                  </VEntityField>
                  <VEntityField v-permission="['system:themes:manage']">
                    <template #description>
                      <VButton
                        size="sm"
                        :disabled="creating"
                        @click="handleCreateTheme(theme)"
                      >
                        {{ $t("core.common.buttons.install") }}
                      </VButton>
                    </template>
                  </VEntityField>
                </template>
              </VEntity>
            </li>
          </ul>
        </Transition>
      </VTabItem>
    </VTabs>

    <template #footer>
      <VSpace>
        <VButton
          v-permission="['system:themes:manage']"
          type="secondary"
          @click="handleOpenInstallModal()"
        >
          {{ $t("core.theme.common.buttons.install") }}
        </VButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>

  <ThemeUploadModal
    v-if="visible"
    v-model:visible="themeUploadVisible"
    :upgrade-theme="themeToUpgrade"
    @close="refetch"
  />

  <ThemePreviewModal
    v-if="visible"
    v-model:visible="previewVisible"
    :theme="selectedPreviewTheme"
  />
</template>
