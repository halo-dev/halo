<script lang="ts" setup>
import { useGlobalInfoStore } from "@/stores/global-info";
import { useUserStore } from "@/stores/user";
import {
  consoleApiClient,
  coreApiClient,
  type Category,
  type Plugin,
  type PostRequest,
  type SinglePageRequest,
  type Tag,
} from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import category from "./setup-data/category.json";
import menuItems from "./setup-data/menu-items.json";
import menu from "./setup-data/menu.json";
import post from "./setup-data/post.json";
import singlePage from "./setup-data/singlePage.json";
import tag from "./setup-data/tag.json";

const router = useRouter();
const globalInfoStore = useGlobalInfoStore();

const { mutateAsync: pluginInstallMutate } = useMutation({
  mutationKey: ["plugin-install"],
  mutationFn: async (plugin: Plugin) => {
    const { data } = await consoleApiClient.plugin.plugin.installPlugin(
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
    return await consoleApiClient.plugin.plugin.changePluginRunningState(
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
    await coreApiClient.content.category.createCategory({
      category: category as Category,
    });
    await coreApiClient.content.tag.createTag({
      tag: tag as Tag,
    });
    const { data: postData } = await consoleApiClient.content.post.draftPost({
      postRequest: post as PostRequest,
    });
    await consoleApiClient.content.post.publishPost({
      name: postData.metadata.name,
    });

    // Create singlePage
    const { data: singlePageData } =
      await consoleApiClient.content.singlePage.draftSinglePage({
        singlePageRequest: singlePage as SinglePageRequest,
      });

    await consoleApiClient.content.singlePage.publishSinglePage({
      name: singlePageData.metadata.name,
    });

    // Create menu and menu items
    const menuItemPromises = menuItems.map((item) => {
      return coreApiClient.menuItem.createMenuItem({
        menuItem: item,
      });
    });
    await Promise.all(menuItemPromises);
    await coreApiClient.menu.createMenu({ menu: menu });

    // Install preset plugins
    const { data: presetPlugins } =
      await consoleApiClient.plugin.plugin.listPluginPresets();

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
    await coreApiClient.configMap.createConfigMap({
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
