<script lang="ts" setup>
import {
  IconAddCircle,
  IconGitHub,
  useDialog,
  VButton,
  VEmpty,
  VModal,
  VSpace,
  VTag,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import ThemeInstallModal from "./ThemeInstallModal.vue";
import { ref, watch } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    selectedTheme: Theme | null;
    activatedTheme: Theme | null;
  }>(),
  {
    visible: false,
    selectedTheme: null,
    activatedTheme: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "update:selectedTheme", theme: Theme | null): void;
  (event: "select", theme: Theme | null): void;
}>();

const themes = ref<Theme[]>([]);
const loading = ref(false);
const themeInstall = ref(false);

const dialog = useDialog();

const handleFetchThemes = async () => {
  try {
    loading.value = true;
    const { data } =
      await apiClient.extension.theme.listthemeHaloRunV1alpha1Theme();
    themes.value = data.items;
  } catch (e) {
    console.error("Failed to fetch themes", e);
  } finally {
    loading.value = false;
  }
};

const handleUninstall = async (theme: Theme) => {
  dialog.warning({
    title: "是否确定删除该主题？",
    description: "删除后将无法恢复。",
    onConfirm: async () => {
      try {
        await apiClient.extension.theme.deletethemeHaloRunV1alpha1Theme({
          name: theme.metadata.name,
        });
      } catch (e) {
        console.error("Failed to uninstall theme", e);
      } finally {
        await handleFetchThemes();
      }
    },
  });
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
    }
  }
);

defineExpose({
  handleFetchThemes,
});
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :width="888"
    height="calc(100vh - 20px)"
    title="已安装的主题"
    @update:visible="onVisibleChange"
  >
    <VEmpty
      v-if="!themes.length && !loading"
      message="当前没有已安装的主题，你可以尝试刷新或者安装新主题"
      title="当前没有已安装的主题"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchThemes"> 刷新</VButton>
          <VButton type="primary" @click="themeInstall = true">
            <template #icon>
              <IconAddCircle class="h-full w-full" />
            </template>
            安装主题
          </VButton>
        </VSpace>
      </template>
    </VEmpty>

    <ul
      v-else
      class="box-border h-full w-full divide-y divide-gray-100"
      role="list"
    >
      <li
        v-for="(theme, index) in themes"
        :key="index"
        @click="handleSelectTheme(theme)"
      >
        <VEntity
          :is-selected="theme.metadata.name === selectedTheme?.metadata?.name"
        >
          <template #start>
            <VEntityField>
              <template #description>
                <div class="w-32">
                  <div
                    class="group aspect-w-4 aspect-h-3 block w-full overflow-hidden rounded border bg-gray-100"
                  >
                    <LazyImage
                      :src="theme.spec.logo"
                      :alt="theme.spec.displayName"
                      classes="pointer-events-none object-cover group-hover:opacity-75"
                    >
                      <template #loading>
                        <div
                          class="flex h-full items-center justify-center object-cover"
                        >
                          <span class="text-xs text-gray-400">加载中...</span>
                        </div>
                      </template>
                      <template #error>
                        <div
                          class="flex h-full items-center justify-center object-cover"
                        >
                          <span class="text-xs text-red-400">加载异常</span>
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
                  v-if="theme.metadata.name === activatedTheme?.metadata?.name"
                >
                  当前启用
                </VTag>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="theme.metadata.deletionTimestamp">
              <template #description>
                <VStatusDot v-tooltip="`删除中`" state="warning" animate />
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
          <template #dropdownItems>
            <VButton
              v-close-popper
              block
              type="danger"
              @click="handleUninstall(theme)"
            >
              卸载
            </VButton>
          </template>
        </VEntity>
      </li>
    </ul>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="themeInstall = true">
          安装主题
        </VButton>
        <VButton @click="onVisibleChange(false)">关闭</VButton>
      </VSpace>
    </template>
  </VModal>

  <ThemeInstallModal
    v-model:visible="themeInstall"
    @close="handleFetchThemes"
  />
</template>
