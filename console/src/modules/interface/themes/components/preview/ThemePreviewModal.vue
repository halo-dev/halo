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
import { computed, markRaw, ref, toRaw, watch } from "vue";
import { useI18n } from "vue-i18n";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { useQuery } from "@tanstack/vue-query";

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

const { t } = useI18n();

interface SettingTab {
  id: string;
  label: string;
}

const { activatedTheme } = storeToRefs(useThemeStore());

const previewFrame = ref<HTMLIFrameElement | null>(null);
const themesVisible = ref(false);
const switching = ref(false);
const selectedTheme = ref<Theme>();

const { data: themes } = useQuery<Theme[]>({
  queryKey: ["themes"],
  queryFn: async () => {
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: false,
    });
    return data.items;
  },
  enabled: computed(() => props.visible),
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      selectedTheme.value = props.theme || activatedTheme?.value;
    } else {
      setTimeout(() => {
        themesVisible.value = false;
        settingsVisible.value = false;
      }, 200);
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
  return t("core.theme.preview_model.title", {
    display_name: selectedTheme.value?.spec.displayName,
  });
});

// theme settings
const saving = ref(false);
const settingTabs = ref<SettingTab[]>([] as SettingTab[]);
const activeSettingTab = ref("");
const settingsVisible = ref(false);

const { data: setting } = useQuery<Setting>({
  queryKey: ["theme-setting", selectedTheme],
  queryFn: async () => {
    const { data } = await apiClient.theme.fetchThemeSetting({
      name: selectedTheme?.value?.metadata.name as string,
    });

    return data;
  },
  onSuccess(data) {
    if (data) {
      const { forms } = data.spec;
      settingTabs.value = forms.map((item: SettingForm) => {
        return {
          id: item.group,
          label: item.label || "",
        };
      });
    }

    activeSettingTab.value = settingTabs.value[0].id;
  },
  enabled: computed(
    () => props.visible && !!selectedTheme.value?.spec.settingName
  ),
});

const { data: configMap, refetch: handleFetchConfigMap } = useQuery<ConfigMap>({
  queryKey: ["theme-configMap", selectedTheme],
  queryFn: async () => {
    const { data } = await apiClient.theme.fetchThemeConfig({
      name: selectedTheme?.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(
    () => !!setting.value && !!selectedTheme.value?.spec.configMapName
  ),
});

const { formSchema, configMapFormData, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  activeSettingTab
);

const handleSaveConfigMap = async () => {
  saving.value = true;

  const configMapToUpdate = convertToSave();

  if (!configMapToUpdate || !selectedTheme?.value) {
    saving.value = false;
    return;
  }

  await apiClient.theme.updateThemeConfig({
    name: selectedTheme?.value?.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  await handleFetchConfigMap();

  saving.value = false;

  handleRefresh();
};

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
        v-tooltip="{
          content: $t('core.theme.empty.actions.switch'),
          delay: 300,
        }"
        :class="{ 'bg-gray-200': themesVisible }"
        @click="handleOpenThemes"
      >
        <IconPalette />
      </span>
      <span
        v-tooltip="{
          content: $t('core.theme.preview_model.actions.setting'),
          delay: 300,
        }"
        :class="{ 'bg-gray-200': settingsVisible }"
        @click="handleOpenSettings(undefined)"
      >
        <IconSettings />
      </span>
      <span
        v-tooltip="{
          content: $t('core.common.buttons.refresh'),
          delay: 300,
        }"
        @click="handleRefresh"
      >
        <IconRefreshLine />
      </span>
      <span
        v-tooltip="{
          content: $t('core.theme.preview_model.actions.open'),
          delay: 300,
        }"
      >
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
        <OverlayScrollbarsComponent
          v-if="themesVisible || settingsVisible"
          element="div"
          :options="{ scrollbars: { autoHide: 'scroll' } }"
          class="relative h-full w-96"
          :class="{ '!overflow-hidden': switching }"
          defer
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
                      configMapFormData[tab.id] &&
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
                      :schema="toRaw(formSchema)"
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
                      {{ $t("core.common.buttons.save") }}
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
        </OverlayScrollbarsComponent>
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
