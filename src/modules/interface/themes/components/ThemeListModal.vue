<script lang="ts" setup>
import {
  IconGitHub,
  IconMore,
  useDialog,
  VButton,
  VModal,
  VSpace,
  VTag,
} from "@halo-dev/components";
import ThemeInstallModal from "./ThemeInstallModal.vue";
import type { PropType } from "vue";
import { onMounted, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedTheme: {
    type: Object as PropType<Theme | null>,
    default: null,
  },
  activatedTheme: {
    type: Object as PropType<Theme | null>,
    default: null,
  },
});

const emit = defineEmits(["update:visible", "close", "update:selectedTheme"]);

const themes = ref<Theme[]>([]);
const themeInstall = ref(false);

const dialog = useDialog();

const handleFetchThemes = async () => {
  try {
    const { data } =
      await apiClient.extension.theme.listthemeHaloRunV1alpha1Theme();
    themes.value = data.items;
  } catch (e) {
    console.error("Failed to fetch themes", e);
  }
};

const handleUninstall = async (theme: Theme) => {
  dialog.warning({
    title: "是否确定删除该主题？",
    description: "删除后将无法恢复。",
    onConfirm: async () => {
      try {
        await apiClient.extension.theme.deletethemeHaloRunV1alpha1Theme(
          theme.metadata.name
        );
      } catch (e) {
        console.error("Failed to uninstall theme", e);
      } finally {
        await handleFetchThemes();
      }
    },
  });
};

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleSelectTheme = (theme: Theme) => {
  emit("update:selectedTheme", theme);
  handleVisibleChange(false);
};

onMounted(handleFetchThemes);

defineExpose({
  handleFetchThemes,
});
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :width="888"
    title="已安装的主题"
    @update:visible="handleVisibleChange"
  >
    <ul class="flex flex-col divide-y divide-gray-100" role="list">
      <li
        v-for="(theme, index) in themes"
        :key="index"
        :class="{
          'bg-gray-50': theme.metadata.name === selectedTheme?.metadata?.name,
        }"
        class="relative cursor-pointer py-4 transition-all hover:bg-gray-100"
        @click="handleSelectTheme(theme)"
      >
        <div class="flex items-center">
          <div
            v-show="theme.metadata.name === selectedTheme?.metadata?.name"
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
                <VTag
                  v-if="theme.metadata.name === activatedTheme?.metadata?.name"
                >
                  当前启用
                </VTag>
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
              <div v-if="theme.spec.repo">
                <a
                  :href="theme.spec.repo"
                  class="text-gray-900 hover:text-blue-600"
                  target="_blank"
                >
                  <IconGitHub />
                </a>
              </div>
              <div>
                <FloatingDropdown>
                  <IconMore
                    class="rounded text-gray-900 hover:bg-gray-200 hover:text-gray-600"
                    @click.stop
                  />
                  <template #popper>
                    <div class="w-48 p-2">
                      <VSpace class="w-full" direction="column">
                        <VButton
                          v-close-popper
                          :disabled="
                            theme.metadata.name ===
                            activatedTheme?.metadata?.name
                          "
                          block
                          type="danger"
                          @click="handleUninstall(theme)"
                        >
                          卸载
                        </VButton>
                      </VSpace>
                    </div>
                  </template>
                </FloatingDropdown>
              </div>
            </VSpace>
          </div>
        </div>
      </li>
    </ul>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="themeInstall = true">
          安装主题
        </VButton>
        <VButton @click="handleVisibleChange(false)">关闭</VButton>
      </VSpace>
    </template>
  </VModal>

  <ThemeInstallModal
    v-model:visible="themeInstall"
    @close="handleFetchThemes"
  />
</template>
