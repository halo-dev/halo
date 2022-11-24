<script lang="ts" setup>
import {
  IconAddCircle,
  IconGitHub,
  Dialog,
  VButton,
  VEmpty,
  VModal,
  VSpace,
  VTag,
  VEntity,
  VEntityField,
  VStatusDot,
  VTabItem,
  VTabs,
  VLoading,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import ThemePreviewModal from "./preview/ThemePreviewModal.vue";
import ThemeUploadModal from "./ThemeUploadModal.vue";
import { computed, ref, watch } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import { onBeforeRouteLeave } from "vue-router";
import { useThemeStore } from "@/stores/theme";
import { storeToRefs } from "pinia";

const { currentUserHasPermission } = usePermission();

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
const themes = ref<Theme[]>([] as Theme[]);
const loading = ref(false);
const themeInstall = ref(false);
const creating = ref(false);
const refreshInterval = ref();

const modalTitle = computed(() => {
  return activeTab.value === "installed" ? "已安装的主题" : "未安装的主题";
});

const { activatedTheme } = storeToRefs(useThemeStore());

const handleFetchThemes = async () => {
  try {
    clearInterval(refreshInterval.value);

    loading.value = true;
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: activeTab.value !== "installed",
    });
    themes.value = data.items;

    if (activeTab.value !== "installed") {
      return;
    }

    const deletedThemes = themes.value.filter(
      (theme) => !!theme.metadata.deletionTimestamp
    );

    if (deletedThemes.length) {
      refreshInterval.value = setInterval(() => {
        handleFetchThemes();
      }, 3000);
    }
  } catch (e) {
    console.error("Failed to fetch themes", e);
  } finally {
    loading.value = false;
  }
};

onBeforeRouteLeave(() => {
  clearInterval(refreshInterval.value);
});

watch(
  () => activeTab.value,
  () => {
    handleFetchThemes();
  }
);

const handleUninstall = async (theme: Theme, deleteExtensions?: boolean) => {
  Dialog.warning({
    title: `${
      deleteExtensions
        ? "是否确认删除该主题以及对应的配置？"
        : "是否确认删除该主题？"
    }`,
    description: "删除后将无法恢复。",
    onConfirm: async () => {
      try {
        await apiClient.extension.theme.deletethemeHaloRunV1alpha1Theme({
          name: theme.metadata.name,
        });

        // delete theme setting and configMap
        if (!deleteExtensions) {
          return;
        }

        const { settingName, configMapName } = theme.spec;

        if (settingName) {
          await apiClient.extension.setting.deletev1alpha1Setting({
            name: settingName,
          });
        }

        if (configMapName) {
          await apiClient.extension.configMap.deletev1alpha1ConfigMap({
            name: configMapName,
          });
        }
      } catch (e) {
        console.error("Failed to uninstall theme", e);
      } finally {
        await handleFetchThemes();
      }
    },
  });
};

const handleCreateTheme = async (theme: Theme) => {
  try {
    creating.value = true;

    const { data } =
      await apiClient.extension.theme.createthemeHaloRunV1alpha1Theme({
        theme,
      });

    // create theme settings
    apiClient.theme.reloadThemeSetting({ name: data.metadata.name });

    activeTab.value = "installed";
  } catch (error) {
    console.error("Failed to create theme", error);
  } finally {
    creating.value = false;
    handleFetchThemes();
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

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      handleFetchThemes();
    } else {
      clearInterval(refreshInterval.value);
    }
  }
);

defineExpose({
  handleFetchThemes,
});

const previewVisible = ref(false);
const selectedPreviewTheme = ref<Theme>();

const handleOpenPreview = (theme: Theme) => {
  selectedPreviewTheme.value = theme;
  previewVisible.value = true;
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
      class="my-[12px] mx-[16px]"
    >
      <VTabItem id="installed" label="已安装" class="-mx-[16px]">
        <VLoading v-if="loading" />
        <Transition v-else-if="!themes.length" appear name="fade">
          <VEmpty
            message="当前没有已安装的主题，你可以尝试刷新或者安装新主题"
            title="当前没有已安装的主题"
          >
            <template #actions>
              <VSpace>
                <VButton :loading="loading" @click="handleFetchThemes">
                  刷新
                </VButton>
                <VButton
                  v-permission="['system:themes:manage']"
                  type="primary"
                  @click="themeInstall = true"
                >
                  <template #icon>
                    <IconAddCircle class="h-full w-full" />
                  </template>
                  安装主题
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
              <VEntity
                :is-selected="
                  theme.metadata.name === selectedTheme?.metadata?.name
                "
              >
                <template #start>
                  <VEntityField>
                    <template #description>
                      <div class="w-32">
                        <div
                          class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
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
                                <span class="text-xs text-gray-400"
                                  >加载中...</span
                                >
                              </div>
                            </template>
                            <template #error>
                              <div
                                class="flex h-full items-center justify-center object-cover"
                              >
                                <span class="text-xs text-red-400"
                                  >加载异常</span
                                >
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
                    <template #extra>
                      <VTag
                        v-if="
                          theme.metadata.name === activatedTheme?.metadata?.name
                        "
                      >
                        当前启用
                      </VTag>
                    </template>
                  </VEntityField>
                </template>
                <template #end>
                  <VEntityField v-if="theme.metadata.deletionTimestamp">
                    <template #description>
                      <VStatusDot
                        v-tooltip="`删除中`"
                        state="warning"
                        animate
                      />
                    </template>
                  </VEntityField>
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
                </template>
                <template
                  v-if="currentUserHasPermission(['system:themes:manage'])"
                  #dropdownItems
                >
                  <VButton
                    v-close-popper
                    block
                    type="secondary"
                    @click="handleOpenPreview(theme)"
                  >
                    预览
                  </VButton>
                  <VButton
                    v-close-popper
                    block
                    type="danger"
                    @click="handleUninstall(theme)"
                  >
                    卸载
                  </VButton>
                  <VButton
                    v-close-popper
                    :disabled="
                      theme.metadata.name === activatedTheme?.metadata?.name
                    "
                    block
                    type="danger"
                    @click="handleUninstall(theme, true)"
                  >
                    卸载并删除配置
                  </VButton>
                </template>
              </VEntity>
            </li>
          </ul>
        </Transition>
      </VTabItem>
      <VTabItem id="uninstalled" label="未安装" class="-mx-[16px]">
        <VLoading v-if="loading" />
        <Transition v-else-if="!themes.length" appear name="fade">
          <VEmpty title="当前没有未安装的主题">
            <template #actions>
              <VSpace>
                <VButton :loading="loading" @click="handleFetchThemes">
                  刷新
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
                          class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
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
                                <span class="text-xs text-gray-400"
                                  >加载中...</span
                                >
                              </div>
                            </template>
                            <template #error>
                              <div
                                class="flex h-full items-center justify-center object-cover"
                              >
                                <span class="text-xs text-red-400"
                                  >加载异常</span
                                >
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
                        安装
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
          @click="themeInstall = true"
        >
          安装主题
        </VButton>
        <VButton @click="onVisibleChange(false)">关闭</VButton>
      </VSpace>
    </template>
  </VModal>

  <ThemeUploadModal
    v-if="visible"
    v-model:visible="themeInstall"
    @close="handleFetchThemes"
  />

  <ThemePreviewModal
    v-if="visible"
    v-model:visible="previewVisible"
    :theme="selectedPreviewTheme"
  />
</template>
