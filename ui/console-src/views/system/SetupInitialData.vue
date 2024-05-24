<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { VLoading } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import {
  type Category,
  type Plugin,
  type PostRequest,
  type SinglePageRequest,
  type Tag,
} from "@halo-dev/api-client";
import { onMounted } from "vue";
import category from "./setup-data/category.json";
import tag from "./setup-data/tag.json";
import post from "./setup-data/post.json";
import singlePage from "./setup-data/singlePage.json";
import menu from "./setup-data/menu.json";
import menuItems from "./setup-data/menu-items.json";
import { useRouter } from "vue-router";
import { useGlobalInfoStore } from "@/stores/global-info";
import { ref } from "vue";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const globalInfoStore = useGlobalInfoStore();

const { mutateAsync: pluginInstallMutate } = useMutation({
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
  async onSuccess(data) {
    await pluginStartMutate(data);
  },
});

const { mutateAsync: pluginStartMutate } = useMutation({
  mutationKey: ["plugin-start"],
  mutationFn: async (plugin: Plugin) => {
    return await apiClient.plugin.changePluginRunningState(
      {
        name: plugin.metadata.name,
        pluginRunningStateRequest: {
          enable: true,
        },
      },
      { mute: true }
    );
  },
  retry: 3,
  retryDelay: 1000,
});

const processing = ref(false);

async function setupInitialData() {
  try {
    processing.value = true;

    // Create category / tag / post
    await apiClient.extension.category.createContentHaloRunV1alpha1Category({
      category: category as Category,
    });
    await apiClient.extension.tag.createContentHaloRunV1alpha1Tag({
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
      return apiClient.extension.menuItem.createV1alpha1MenuItem({
        menuItem: item,
      });
    });
    await Promise.all(menuItemPromises);
    await apiClient.extension.menu.createV1alpha1Menu({ menu: menu });

    // Install preset plugins
    const { data: presetPlugins } = await apiClient.plugin.listPluginPresets();

    for (let i = 0; i < presetPlugins.length; i++) {
      const presetPlugin = presetPlugins[i];
      try {
        await pluginInstallMutate(presetPlugin);
      } catch (error) {
        console.error("Failed to install plugin: ", presetPlugin.metadata.name);
      }
    }
  } catch (error) {
    console.error(error);
  } finally {
    await apiClient.extension.configMap.createV1alpha1ConfigMap({
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
    processing.value = false;
  }

  await globalInfoStore.fetchGlobalInfo();

  // Reload page to fetch plugin's bundle files
  window.location.reload();
}

const userStore = useUserStore();

onMounted(async () => {
  await globalInfoStore.fetchGlobalInfo();

  if (
    globalInfoStore.globalInfo &&
    globalInfoStore.globalInfo.dataInitialized === false &&
    !userStore.isAnonymous
  ) {
    setupInitialData();
    return;
  }

  router.push({ name: "Dashboard" });
});
</script>

<template>
  <div class="flex h-screen flex-col items-center justify-center">
    <VLoading />
    <div v-if="processing" class="text-xs text-gray-600">
      {{ $t("core.setup.operations.setup_initial_data.loading") }}
    </div>
  </div>
</template>
