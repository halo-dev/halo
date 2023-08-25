<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { VLoading } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import type {
  Category,
  Plugin,
  PostRequest,
  SinglePageRequest,
  Tag,
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

const processing = ref(false);

async function setupInitialData() {
  try {
    processing.value = true;

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
    console.error(error);
  } finally {
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
    processing.value = false;
  }

  await globalInfoStore.fetchGlobalInfo();

  router.push({ name: "Dashboard" });
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
