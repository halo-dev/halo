<script lang="ts" setup>
import ThemePreviewListItem from "./ThemePreviewListItem.vue";
import { useSettingFormConvert } from "@/composables/use-setting-form";
import { useThemeStore } from "@/stores/theme";
import { apiClient } from "@/utils/api-client";
import type {
  ConfigMap,
  Setting,
  SettingForm,
  Theme,
} from "@halo-dev/api-client";
import {
  VModal,
  IconLink,
  IconPalette,
  IconSettings,
  IconArrowLeft,
  VTabbar,
  VButton,
  IconComputer,
  IconPhone,
  IconTablet,
  IconRefreshLine,
  Toast,
} from "@halo-dev/components";
import { storeToRefs } from "pinia";
import { computed, markRaw, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    title?: string;
    theme?: Theme;
  }>(),
  {
    visible: false,
    title: undefined,
    theme: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

interface SettingTab {
  id: string;
  label: string;
}

const { activatedTheme } = storeToRefs(useThemeStore());

const previewFrame = ref<HTMLIFrameElement | null>(null);
const themes = ref<Theme[]>([] as Theme[]);
const themesVisible = ref(false);
const switching = ref(false);
const selectedTheme = ref<Theme>();

const handleFetchThemes = async () => {
  try {
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: false,
    });
    themes.value = data.items;
  } catch (e) {
    console.error("Failed to fetch themes", e);
  }
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      handleFetchThemes();
      selectedTheme.value = props.theme || activatedTheme?.value;
    } else {
      themesVisible.value = false;
      settingsVisible.value = false;
    }
  }
);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleOpenThemes = () => {
  settingsVisible.value = false;
  themesVisible.value = !themesVisible.value;
};

const handleSelect = (theme: Theme) => {
  selectedTheme.value = theme;
};

const previewUrl = computed(() => {
  if (!selectedTheme.value) {
    return "#";
  }
  return `${import.meta.env.VITE_API_URL}/?preview-theme=${
    selectedTheme.value.metadata.name
  }`;
});

const modalTitle = computed(() => {
  if (props.title) {
    return props.title;
  }
  return `预览主题：${selectedTheme.value?.spec.displayName}`;
});

// theme settings
const setting = ref<Setting>();
const configMap = ref<ConfigMap>();
const saving = ref(false);
const settingTabs = ref<SettingTab[]>([] as SettingTab[]);
const activeSettingTab = ref("");
const settingsVisible = ref(false);

const { formSchema, configMapFormData, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  activeSettingTab
);

const handleFetchSettings = async () => {
  if (!selectedTheme?.value) return;

  const { data } = await apiClient.theme.fetchThemeSetting({
    name: selectedTheme?.value?.metadata.name,
  });

  setting.value = data;
};

const handleFetchConfigMap = async () => {
  if (!selectedTheme?.value) return;

  const { data } = await apiClient.theme.fetchThemeConfig({
    name: selectedTheme?.value?.metadata.name,
  });

  configMap.value = data;
};

const handleSaveConfigMap = async () => {
  saving.value = true;

  const configMapToUpdate = convertToSave();

  if (!configMapToUpdate || !selectedTheme?.value) {
    saving.value = false;
    return;
  }

  const { data: newConfigMap } = await apiClient.theme.updateThemeConfig({
    name: selectedTheme?.value?.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success("保存成功");

  await handleFetchSettings();
  configMap.value = newConfigMap;

  saving.value = false;

  handleRefresh();
};

watch(
  () => selectedTheme.value,
  async () => {
    if (selectedTheme.value) {
      await handleFetchSettings();
      await handleFetchConfigMap();

      if (setting.value) {
        const { forms } = setting.value.spec;
        settingTabs.value = forms.map((item: SettingForm) => {
          return {
            id: item.group,
            label: item.label || "",
          };
        });
      }

      activeSettingTab.value = settingTabs.value[0].id;
    }
  }
);

const handleOpenSettings = (theme?: Theme) => {
  if (theme) {
    selectedTheme.value = theme;
  }
  themesVisible.value = false;
  settingsVisible.value = !settingsVisible.value;
};

const handleRefresh = () => {
  previewFrame.value?.contentWindow?.location.reload();
};

// mock devices
const mockDevices = [
  {
    id: "desktop",
    icon: markRaw(IconComputer),
  },
  {
    id: "tablet",
    icon: markRaw(IconTablet),
  },
  {
    id: "phone",
    icon: markRaw(IconPhone),
  },
];

const deviceActiveId = ref(mockDevices[0].id);

const iframeClasses = computed(() => {
  if (deviceActiveId.value === "desktop") {
    return "w-full h-full";
  }
  if (deviceActiveId.value === "tablet") {
    return "w-2/3 h-2/3 ring-2 rounded ring-gray-300";
  }
  return "w-96 h-[50rem] ring-2 rounded ring-gray-300";
});
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    fullscreen
    :title="modalTitle"
    :mount-to-body="true"
    @update:visible="onVisibleChange"
  >
    <template #center>
      <VTabbar
        v-model:active-id="deviceActiveId"
        :items="mockDevices"
        type="outline"
      ></VTabbar>
    </template>
    <template #actions>
      <span
        v-tooltip="{ content: '切换主题', delay: 300 }"
        :class="{ 'bg-gray-200': themesVisible }"
        @click="handleOpenThemes"
      >
        <IconPalette />
      </span>
      <span
        v-tooltip="{ content: '主题设置', delay: 300 }"
        :class="{ 'bg-gray-200': settingsVisible }"
        @click="handleOpenSettings(undefined)"
      >
        <IconSettings />
      </span>
      <span v-tooltip="{ content: '刷新', delay: 300 }" @click="handleRefresh">
        <IconRefreshLine />
      </span>
      <span v-tooltip="{ content: '新窗口打开', delay: 300 }">
        <a :href="previewUrl" target="_blank">
          <IconLink />
        </a>
      </span>
    </template>
    <div
      class="flex h-full items-center justify-center divide-x divide-gray-100 transition-all"
    >
      <transition
        enter-active-class="transform transition ease-in-out duration-300"
        enter-from-class="-translate-x-full"
        enter-to-class="translate-x-0"
        leave-active-class="transform transition ease-in-out duration-300"
        leave-from-class="translate-x-0"
        leave-to-class="-translate-x-full"
        appear
      >
        <div
          v-if="themesVisible || settingsVisible"
          class="relative h-full w-96 overflow-y-auto"
          :class="{ '!overflow-hidden': switching }"
          style="overflow-y: overlay"
        >
          <transition
            enter-active-class="transform transition ease-in-out duration-300 delay-150"
            enter-from-class="translate-x-full"
            enter-to-class="-translate-x-0"
            leave-active-class="transform transition ease-in-out duration-300"
            leave-from-class="-translate-x-0"
            leave-to-class="translate-x-full"
            @before-enter="switching = true"
            @after-enter="switching = false"
            @before-leave="switching = true"
            @after-leave="switching = false"
          >
            <div v-show="settingsVisible" class="mb-20">
              <VTabbar
                v-model:active-id="activeSettingTab"
                :items="settingTabs"
                class="w-full !rounded-none"
                type="outline"
              ></VTabbar>
              <div class="bg-white p-3">
                <div v-for="(tab, index) in settingTabs" :key="index">
                  <FormKit
                    v-if="
                      tab.id === activeSettingTab &&
                      configMapFormData &&
                      formSchema
                    "
                    :id="`preview-setting-${tab.id}`"
                    :key="tab.id"
                    v-model="configMapFormData[tab.id]"
                    :name="tab.id"
                    :actions="false"
                    :preserve="true"
                    type="form"
                    @submit="handleSaveConfigMap"
                  >
                    <FormKitSchema
                      :schema="formSchema"
                      :data="configMapFormData[tab.id]"
                    />
                  </FormKit>
                </div>
                <div v-permission="['system:themes:manage']" class="pt-5">
                  <div class="flex justify-start">
                    <VButton
                      :loading="saving"
                      type="secondary"
                      @click="
                        $formkit.submit(
                          `preview-setting-${activeSettingTab}` || ''
                        )
                      "
                    >
                      保存
                    </VButton>
                  </div>
                </div>
              </div>
            </div>
          </transition>
          <transition
            enter-active-class="transform transition ease-in-out duration-300 delay-150"
            enter-from-class="-translate-x-full"
            enter-to-class="translate-x-0"
            leave-active-class="transform transition ease-in-out duration-300"
            leave-from-class="translate-x-0"
            leave-to-class="-translate-x-full"
            @before-enter="switching = true"
            @after-enter="switching = false"
            @before-leave="switching = true"
            @after-leave="switching = false"
          >
            <ul
              v-show="themesVisible"
              class="box-border h-full w-full divide-y divide-gray-100"
              role="list"
            >
              <li
                v-for="(item, index) in themes"
                :key="index"
                @click="handleSelect(item)"
              >
                <ThemePreviewListItem
                  :theme="item"
                  :is-selected="
                    selectedTheme?.metadata.name === item.metadata.name
                  "
                  @open-settings="handleOpenSettings(item)"
                />
              </li>
            </ul>
          </transition>
          <transition
            enter-active-class="transform transition ease-in-out duration-300"
            enter-from-class="translate-y-full"
            enter-to-class="translate-y-0"
            leave-active-class="transform transition ease-in-out duration-300"
            leave-from-class="translate-y-0"
            leave-to-class="translate-y-full"
          >
            <div v-if="settingsVisible" class="fixed bottom-2 left-2">
              <VButton
                size="md"
                circle
                type="primary"
                @click="handleOpenThemes"
              >
                <IconArrowLeft />
              </VButton>
            </div>
          </transition>
        </div>
      </transition>
      <div
        class="flex h-full flex-1 items-center justify-center transition-all duration-300"
      >
        <iframe
          v-if="visible"
          ref="previewFrame"
          class="border-none transition-all duration-500"
          :class="iframeClasses"
          :src="previewUrl"
        ></iframe>
      </div>
    </div>
  </VModal>
</template>
