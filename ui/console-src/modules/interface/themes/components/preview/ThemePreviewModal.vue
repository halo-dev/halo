<script lang="ts" setup>
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import { useThemeStore } from "@console/stores/theme";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
import type { Setting, SettingForm, Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconComputer,
  IconLink,
  IconPalette,
  IconPhone,
  IconRefreshLine,
  IconSettings,
  IconTablet,
  Toast,
  VButton,
  VEntityContainer,
  VLoading,
  VModal,
  VTabbar,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { cloneDeep, set } from "lodash-es";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { storeToRefs } from "pinia";
import { computed, markRaw, onMounted, ref, toRaw } from "vue";
import { useI18n } from "vue-i18n";
import ThemePreviewListItem from "./ThemePreviewListItem.vue";

const props = withDefaults(
  defineProps<{
    title?: string;
    theme?: Theme;
  }>(),
  {
    title: undefined,
    theme: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const queryClient = useQueryClient();
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
    const { data } = await consoleApiClient.theme.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: false,
    });
    return data.items;
  },
});

onMounted(() => {
  selectedTheme.value = toRaw(props.theme) || toRaw(activatedTheme?.value);
});

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
  return `/?preview-theme=${selectedTheme.value.metadata.name}`;
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
    const { data } = await consoleApiClient.theme.theme.fetchThemeSetting({
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
  enabled: computed(() => !!selectedTheme.value?.spec.settingName),
});

const { data: configMapData } = useQuery({
  queryKey: ["core:theme:configMap:data", selectedTheme],
  queryFn: async () => {
    const { data } = await consoleApiClient.theme.theme.fetchThemeJsonConfig({
      name: selectedTheme?.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(
    () => !!setting.value && !!selectedTheme.value?.spec.configMapName
  ),
});

const currentConfigMapGroupData = computed(() => {
  return configMapData.value?.[activeSettingTab.value];
});

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  const { forms } = setting.value.spec;
  return forms.find((item) => item.group === activeSettingTab.value)
    ?.formSchema as (FormKitSchemaCondition | FormKitSchemaNode)[];
});

const handleRefresh = () => {
  previewFrame.value?.contentWindow?.location.reload();
};

const handleSaveConfigMap = async (data: object) => {
  saving.value = true;

  if (!selectedTheme?.value) {
    saving.value = false;
    return;
  }

  await consoleApiClient.theme.theme.updateThemeJsonConfig({
    name: selectedTheme?.value?.metadata.name,
    body: set(
      cloneDeep(configMapData.value) || {},
      activeSettingTab.value,
      data
    ),
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["core:theme:configMap:data"] });

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
    fullscreen
    :title="modalTitle"
    :mount-to-body="true"
    @close="emit('close')"
  >
    <template #center>
      <!-- TODO: Reactor VTabbar component to support icon prop -->
      <VTabbar
        v-model:active-id="deviceActiveId"
        :items="mockDevices as any"
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
        @click="handleRefresh()"
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
              <!-- @vue-skip -->
              <VTabbar
                v-model:active-id="activeSettingTab"
                :items="settingTabs"
                class="w-full !rounded-none"
                type="outline"
              ></VTabbar>
              <div class="bg-white p-3">
                <FormKit
                  v-if="
                    activeSettingTab && formSchema && currentConfigMapGroupData
                  "
                  :id="activeSettingTab"
                  :key="activeSettingTab"
                  :value="currentConfigMapGroupData || {}"
                  :name="activeSettingTab"
                  :preserve="true"
                  type="form"
                  @submit="handleSaveConfigMap"
                >
                  <FormKitSchema
                    :schema="toRaw(formSchema)"
                    :data="toRaw(currentConfigMapGroupData)"
                  />
                </FormKit>
                <StickyBlock
                  v-permission="['system:themes:manage']"
                  class="-mx-4 -mb-4 -mr-3 rounded-b-base rounded-t-lg bg-white p-4 pt-5"
                  position="bottom"
                >
                  <VButton
                    :loading="saving"
                    type="secondary"
                    @click="$formkit.submit(activeSettingTab)"
                  >
                    {{ $t("core.common.buttons.save") }}
                  </VButton>
                </StickyBlock>
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
            <VEntityContainer v-show="themesVisible">
              <ThemePreviewListItem
                v-for="item in themes"
                :key="item.metadata.name"
                :theme="item"
                :is-selected="
                  selectedTheme?.metadata.name === item.metadata.name
                "
                @click="handleSelect(item)"
                @open-settings="handleOpenSettings(item)"
              />
            </VEntityContainer>
          </transition>
        </OverlayScrollbarsComponent>
      </transition>
      <div
        class="flex h-full flex-1 items-center justify-center transition-all duration-300"
      >
        <VLoading v-if="!previewUrl" />
        <iframe
          v-else
          ref="previewFrame"
          class="border-none transition-all duration-500"
          :class="iframeClasses"
          :src="previewUrl"
        ></iframe>
      </div>
    </div>
  </VModal>
</template>
