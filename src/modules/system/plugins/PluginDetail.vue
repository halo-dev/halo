<script lang="ts" setup>
import {
  useDialog,
  VButton,
  VCard,
  VPageHeader,
  VSwitch,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { useRoute } from "vue-router";
import { computed, onMounted, ref } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type {
  Plugin,
  ConfigMap,
  Setting,
  SettingSpec,
} from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";

interface FormKitSettingSpec extends Omit<SettingSpec, "formSchema"> {
  formSchema: FormKitSchemaCondition | FormKitSchemaNode[];
}

interface FormKitSetting extends Omit<Setting, "spec"> {
  spec: Array<FormKitSettingSpec>;
}

const pageTabs = ref([{ id: "detail", label: "详情" }]);
const activeTabId = ref(pageTabs.value[0].id);
const plugin = ref<Plugin>({} as Plugin);
const settings = ref<FormKitSetting>({} as FormKitSetting);

const configmapFormData = ref<Record<string, Record<string, string>>>({});
const configmap = ref<ConfigMap>({
  data: {},
  apiVersion: "v1alpha1",
  kind: "ConfigMap",
  metadata: {
    name: "",
  },
});
const saving = ref(false);

const { params } = useRoute();
const dialog = useDialog();

const handleFetchPlugin = async () => {
  try {
    const response =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin(
        params.pluginName as string
      );
    plugin.value = response.data;

    await handleFetchSettings();
    await handleFetchConfigMap();
  } catch (e) {
    console.error(e);
  }
};

const handleFetchSettings = async () => {
  if (!plugin.value.spec.settingName) {
    return;
  }
  try {
    const response = await apiClient.extension.setting.getv1alpha1Setting(
      plugin.value.spec.settingName as string
    );
    settings.value = response.data as FormKitSetting;

    const { spec } = settings.value;

    if (spec) {
      pageTabs.value = [
        ...pageTabs.value,
        ...spec.map((item: FormKitSettingSpec) => {
          return {
            id: item.group,
            label: item.label || "",
          };
        }),
      ];

      spec.forEach((item: FormKitSettingSpec) => {
        configmapFormData.value[item.group] = {};
      });
    }
  } catch (e) {
    console.error(e);
  }
};

const handleFetchConfigMap = async () => {
  if (!plugin.value.spec.configMapName) {
    return;
  }
  try {
    const response = await apiClient.extension.configMap.getv1alpha1ConfigMap(
      plugin.value.spec.configMapName as string
    );
    configmap.value = response.data;

    const { data } = configmap.value;

    if (data) {
      Object.keys(data).forEach((key) => {
        configmapFormData.value[key] = JSON.parse(data[key]);
      });
    }
  } catch (e) {
    console.error(e);
  }
};

const handleSaveConfigMap = async () => {
  try {
    saving.value = true;

    if (!configmap.value.metadata.name && plugin.value.spec.configMapName) {
      configmap.value.metadata.name = plugin.value.spec.configMapName;
    }

    settings.value.spec.forEach((item: FormKitSettingSpec) => {
      // @ts-ignore
      configmap.value.data[item.group] = JSON.stringify(
        configmapFormData.value[item.group]
      );
    });

    if (!configmap.value.metadata.creationTimestamp) {
      await apiClient.extension.configMap.createv1alpha1ConfigMap(
        configmap.value
      );
    } else {
      await apiClient.extension.configMap.updatev1alpha1ConfigMap(
        configmap.value.metadata.name,
        configmap.value
      );
    }
  } catch (e) {
    console.error(e);
  } finally {
    await handleFetchConfigMap();
    saving.value = false;
  }
};

const isStarted = computed(() => {
  return plugin.value.status?.phase === "STARTED" && plugin.value.spec.enabled;
});

const handleChangePluginStatus = async () => {
  const pluginToUpdate = cloneDeep(plugin.value);

  dialog.info({
    title: `确定要${plugin.value.spec.enabled ? "停止" : "启动"}该插件吗？`,
    onConfirm: async () => {
      try {
        pluginToUpdate.spec.enabled = !pluginToUpdate.spec.enabled;
        await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin(
          plugin.value.metadata.name,
          pluginToUpdate
        );
      } catch (e) {
        console.error(e);
      } finally {
        window.location.reload();
      }
    },
  });
};

onMounted(handleFetchPlugin);
</script>

<template>
  <VPageHeader :title="plugin?.spec?.displayName">
    <template #icon>
      <img :src="plugin?.spec?.logo" class="mr-2 h-8 w-8" />
    </template>
    <template #actions>
      <VButton class="opacity-0" type="secondary">安装</VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTabId"
          :items="pageTabs"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>

      <div v-if="activeTabId === 'detail'">
        <div class="flex items-center justify-between px-4 py-4 sm:px-6">
          <div>
            <h3 class="text-lg font-medium leading-6 text-gray-900">
              插件信息
            </h3>
            <p
              class="mt-1 flex max-w-2xl items-center gap-2 text-sm text-gray-500"
            >
              <span>{{ plugin?.spec?.version }}</span>
              <VTag>
                {{ isStarted ? "已启用" : "未启用" }}
              </VTag>
            </p>
          </div>
          <div v-permission="['system:plugins:manage']">
            <VSwitch
              :model-value="isStarted"
              @change="handleChangePluginStatus"
            />
          </div>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">名称</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ plugin?.spec?.displayName }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">版本</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ plugin?.spec?.version }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ plugin?.spec?.requires }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">提供方</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <a :href="plugin?.spec?.homepage" target="_blank">
                  {{ plugin?.spec?.author }}
                </a>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">协议</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul
                  v-if="plugin?.spec?.license && plugin?.spec?.license.length"
                  class="list-inside list-disc"
                >
                  <li
                    v-for="(license, index) in plugin.spec.license"
                    :key="index"
                  >
                    <a v-if="license.url" :href="license.url" target="_blank">
                      {{ license.name }}
                    </a>
                    <span>
                      {{ license.name }}
                    </span>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">模型定义</dt>
              <dd class="mt-1 sm:col-span-2 sm:mt-0">
                <span>无</span>
              </dd>
            </div>
            <div
              class="bg-gray-50 px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">权限定义</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-5 sm:mt-0">
                <dl class="divide-y divide-gray-100">
                  <div
                    class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
                  >
                    <dt class="text-sm font-medium text-gray-900">
                      Discussions Management
                    </dt>
                    <dd
                      class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0"
                    >
                      <ul class="space-y-2">
                        <li>
                          <div
                            class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                          >
                            <input
                              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                              type="checkbox"
                            />
                            <div class="inline-flex flex-col gap-y-3">
                              <span class="font-medium text-gray-900">
                                Discussions Management
                              </span>
                              <span class="text-xs text-gray-400">
                                依赖于 Discussions View
                              </span>
                            </div>
                          </div>
                        </li>
                        <li>
                          <div
                            class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                          >
                            <input
                              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                              type="checkbox"
                            />
                            <div class="inline-flex flex-col gap-y-3">
                              <span class="font-medium text-gray-900">
                                Discussions View
                              </span>
                            </div>
                          </div>
                        </li>
                      </ul>
                    </dd>
                  </div>

                  <div
                    class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
                  >
                    <dt class="text-sm font-medium text-gray-900">
                      Posts Management
                    </dt>
                    <dd
                      class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0"
                    >
                      <ul class="space-y-2">
                        <li>
                          <div
                            class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                          >
                            <input
                              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                              type="checkbox"
                            />
                            <div class="inline-flex flex-col gap-y-3">
                              <span class="font-medium text-gray-900">
                                Posts Management
                              </span>
                              <span class="text-xs text-gray-400">
                                依赖于 Posts View
                              </span>
                            </div>
                          </div>
                        </li>
                        <li>
                          <div
                            class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                          >
                            <input
                              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                              type="checkbox"
                            />
                            <div class="inline-flex flex-col gap-y-3">
                              <span class="font-medium text-gray-900">
                                Posts View
                              </span>
                            </div>
                          </div>
                        </li>
                      </ul>
                    </dd>
                  </div>
                </dl>
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <template v-for="(group, index) in settings.spec">
        <div
          v-if="activeTabId === group.group"
          :key="index"
          class="p-4 sm:px-6"
        >
          <div class="w-1/3">
            <FormKit
              :id="group.group"
              v-model="configmapFormData[group.group]"
              :actions="false"
              :preserve="true"
              type="form"
              @submit="handleSaveConfigMap"
            >
              <FormKitSchema :schema="group.formSchema" />
            </FormKit>
          </div>
          <div class="pt-5">
            <div class="flex justify-start">
              <VButton
                :loading="saving"
                type="secondary"
                @click="$formkit.submit(group.group)"
              >
                保存
              </VButton>
            </div>
          </div>
        </div>
      </template>
    </VCard>
  </div>
</template>
