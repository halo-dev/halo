<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VEmpty,
  VSpace,
  VLoading,
} from "@halo-dev/components";
import ThemePreviewModal from "../preview/ThemePreviewModal.vue";
import ThemeListItem from "../ThemeListItem.vue";
import { ref, inject, type Ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";
import { useThemeStore } from "@/stores/theme";

const themeStore = useThemeStore();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());
const activeTabId = inject<Ref<string>>("activeTabId", ref(""));

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
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: false,
    });
    return data.items.sort((a, b) => {
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
    const deletingThemes = data?.filter(
      (theme) => !!theme.metadata.deletionTimestamp
    );

    return deletingThemes?.length ? 1000 : false;
  },
});

// preview
const previewVisible = ref(false);
const selectedPreviewTheme = ref<Theme>();

const handleOpenPreview = (theme: Theme) => {
  selectedPreviewTheme.value = theme;
  previewVisible.value = true;
};
</script>

<template>
  <div id="installed-themes-wrapper">
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
              @click="activeTabId = 'local-upload'"
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
      <ul class="box-border h-full w-full space-y-3" role="list">
        <li v-for="(theme, index) in themes" :key="index">
          <ThemeListItem
            :theme="theme"
            :is-selected="theme.metadata.name === selectedTheme?.metadata?.name"
            @select="handleSelectTheme"
            @preview="handleOpenPreview(theme)"
          />
        </li>
      </ul>
    </Transition>
    <ThemePreviewModal
      v-model:visible="previewVisible"
      :theme="selectedPreviewTheme"
    />
  </div>
</template>
