<script lang="ts" setup>
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import { useSettingForm } from "@/composables/use-setting-form";
import { useSystemStatesStore } from "@/stores/system-states";
import { apiClient } from "@/utils/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import category from "./setup-data/category.json";
import tag from "./setup-data/tag.json";
import post from "./setup-data/post.json";
import singlePage from "./setup-data/singlePage.json";
import menu from "./setup-data/menu.json";
import menuItems from "./setup-data/menu-items.json";
import type {
  Category,
  Plugin,
  PostRequest,
  SinglePageRequest,
  Tag,
} from "@halo-dev/api-client";
import { useThemeStore } from "@/stores/theme";
import { useMutation } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const router = useRouter();
const { t } = useI18n();

const {
  configMapFormData,
  handleSaveConfigMap,
  handleFetchSettings,
  handleFetchConfigMap,
} = useSettingForm(ref("system"), ref("system"));

const siteTitle = ref("");
const loading = ref(false);

const { mutate: pluginInstallMutate } = useMutation({
  mutationKey: ["plugin-install"],
  mutationFn: async (plugin: Plugin) => {
    const { data } = await apiClient.plugin.installPlugin(
      {
        source: "PRESET",
        presetName: plugin.metadata.name as string,
      },
      {
        mute: true,
      }
    );
    return data;
  },
  retry: 3,
  retryDelay: 1000,
  onSuccess(data) {
    pluginStartMutate(data);
  },
});

const { mutate: pluginStartMutate } = useMutation({
  mutationKey: ["plugin-start"],
  mutationFn: async (plugin: Plugin) => {
    const { data: pluginToUpdate } =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
        name: plugin.metadata.name,
      });

    pluginToUpdate.spec.enabled = true;

    return apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin(
      {
        name: plugin.metadata.name,
        plugin: pluginToUpdate,
      },
      {
        mute: true,
      }
    );
  },
  retry: 3,
  retryDelay: 1000,
});

const handleSubmit = async () => {
  try {
    loading.value = true;

    // Set site title
    if (configMapFormData.value) {
      configMapFormData.value["basic"].title = siteTitle.value;
      await handleSaveConfigMap();
    }

    // Create category / tag / post
    await apiClient.extension.category.createcontentHaloRunV1alpha1Category({
      category: category as Category,
    });
    await apiClient.extension.tag.createcontentHaloRunV1alpha1Tag({
      tag: tag as Tag,
    });
    const { data: postData } = await apiClient.post.draftPost({
      postRequest: post as PostRequest,
    });
    await apiClient.post.publishPost({ name: postData.metadata.name });

    // Create singlePage
    const { data: singlePageData } = await apiClient.singlePage.draftSinglePage(
      {
        singlePageRequest: singlePage as SinglePageRequest,
      }
    );

    await apiClient.singlePage.publishSinglePage({
      name: singlePageData.metadata.name,
    });

    // Create menu and menu items
    const menuItemPromises = menuItems.map((item) => {
      return apiClient.extension.menuItem.createv1alpha1MenuItem({
        menuItem: item,
      });
    });
    await Promise.all(menuItemPromises);
    await apiClient.extension.menu.createv1alpha1Menu({ menu: menu });

    // Install preset plugins
    const { data: presetPlugins } = await apiClient.plugin.listPluginPresets();

    for (let i = 0; i < presetPlugins.length; i++) {
      pluginInstallMutate(presetPlugins[i]);
    }
  } catch (error) {
    console.error("Failed to initialize preset data", error);
  }

  // Create system-states ConfigMap
  await apiClient.extension.configMap.createv1alpha1ConfigMap({
    configMap: {
      metadata: {
        name: "system-states",
      },
      kind: "ConfigMap",
      apiVersion: "v1alpha1",
      data: {
        states: JSON.stringify({ isSetup: true }),
      },
    },
  });

  const systemStateStore = useSystemStatesStore();
  await systemStateStore.fetchSystemStates();
  const themeStore = useThemeStore();
  await themeStore.fetchActivatedTheme();

  loading.value = false;

  router.push({ name: "Dashboard" });

  Toast.success(t("core.setup.operations.submit.toast_success"));
};

onMounted(async () => {
  const systemStatesStore = useSystemStatesStore();

  if (systemStatesStore.states.isSetup) {
    router.push({ name: "Dashboard" });
    return;
  }

  handleFetchSettings();
  handleFetchConfigMap();
});
</script>

<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <IconLogo class="mb-8" />
    <div class="flex w-72 flex-col gap-4">
      <FormKit
        id="setup-form"
        name="setup-form"
        :actions="false"
        type="form"
        @submit="handleSubmit"
        @keyup.enter="$formkit.submit('setup-form')"
      >
        <FormKit
          v-model="siteTitle"
          :validation-messages="{
            required: $t('core.setup.fields.site_title.validation'),
          }"
          type="text"
          :placeholder="$t('core.setup.fields.site_title.placeholder')"
          validation="required|length:0,100"
        ></FormKit>
      </FormKit>
      <VButton
        block
        type="secondary"
        :loading="loading"
        @click="$formkit.submit('setup-form')"
      >
        {{ $t("core.setup.operations.submit.button") }}
      </VButton>
    </div>
  </div>
</template>
