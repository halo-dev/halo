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
  PostRequest,
  SinglePageRequest,
  Tag,
} from "@halo-dev/api-client";
import { useThemeStore } from "@/stores/theme";

const router = useRouter();

const {
  configMapFormData,
  handleSaveConfigMap,
  handleFetchSettings,
  handleFetchConfigMap,
} = useSettingForm(ref("system"), ref("system"));

const siteTitle = ref("");
const loading = ref(false);

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

    try {
      await apiClient.post.publishPost({ name: postData.metadata.name });
    } catch (e) {
      console.error("Publish post failed", e);
    }

    // Create singlePage
    const { data: singlePageData } = await apiClient.singlePage.draftSinglePage(
      {
        singlePageRequest: singlePage as SinglePageRequest,
      }
    );

    try {
      await apiClient.singlePage.publishSinglePage({
        name: singlePageData.metadata.name,
      });
    } catch (e) {
      console.error("Publish single page failed", e);
    }

    // Create menu and menu items
    const menuItemPromises = menuItems.map((item) => {
      return apiClient.extension.menuItem.createv1alpha1MenuItem({
        menuItem: item,
      });
    });
    await Promise.all(menuItemPromises);
    await apiClient.extension.menu.createv1alpha1Menu({ menu: menu });

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

    router.push({ name: "Dashboard" });

    Toast.success("初始化成功");
  } catch (error) {
    console.error("Failed to setup", error);
  } finally {
    loading.value = false;
  }
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
            required: '请输入站点名称',
          }"
          type="text"
          placeholder="站点名称"
          validation="required|length:0,100"
        ></FormKit>
      </FormKit>
      <VButton
        block
        type="secondary"
        :loading="loading"
        @click="$formkit.submit('setup-form')"
      >
        初始化
      </VButton>
    </div>
  </div>
</template>
