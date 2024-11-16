<script lang="ts" setup>
import { useSettingFormConvert } from "@console/composables/use-setting-form";
import type { AuthProvider, Setting } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Toast,
  VAvatar,
  VButton,
  VCard,
  VDescription,
  VDescriptionItem,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref, toRaw } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";

const route = useRoute();
const { t } = useI18n();

const tabs = ref<{ id: string; label: string }[]>([
  {
    id: "detail",
    label: t("core.identity_authentication.tabs.detail"),
  },
]);

const activeTab = ref<string>("detail");

const { data: authProvider } = useQuery<AuthProvider>({
  queryKey: ["auth-provider", route.params.name],
  queryFn: async () => {
    const { data } = await coreApiClient.auth.authProvider.getAuthProvider({
      name: route.params.name as string,
    });
    return data;
  },
  onSuccess(data) {
    if (data.spec.settingRef?.name) {
      tabs.value.push({
        id: "setting",
        label: t("core.identity_authentication.tabs.setting"),
      });
    }
  },
  enabled: computed(() => !!route.params.name),
});

// setting
const saving = ref(false);
const group = computed(
  () => authProvider.value?.spec.settingRef?.group as string
);

const { data: setting, refetch: handleFetchSettings } = useQuery<Setting>({
  queryKey: [
    "auth-provider-setting",
    authProvider.value?.spec.settingRef?.name,
  ],
  queryFn: async () => {
    const { data } = await coreApiClient.setting.getSetting(
      {
        name: authProvider.value?.spec.settingRef?.name as string,
      },
      {
        mute: true,
      }
    );
    return data;
  },
  enabled: computed(() => !!authProvider.value?.spec.settingRef?.name),
});

const { data: configMap, refetch: handleFetchConfigMap } = useQuery({
  queryKey: [
    "auth-provider-configMap",
    authProvider.value?.spec.configMapRef?.name,
  ],
  queryFn: async () => {
    const { data } = await coreApiClient.configMap.getConfigMap(
      {
        name: authProvider.value?.spec.configMapRef?.name as string,
      },
      {
        mute: true,
      }
    );
    return data;
  },
  retry: 0,
  onError: async () => {
    const data = {};
    data[group.value] = "";
    await coreApiClient.configMap.createConfigMap({
      configMap: {
        apiVersion: "v1alpha1",
        data: data,
        kind: "ConfigMap",
        metadata: {
          name: authProvider.value?.spec.configMapRef?.name as string,
        },
      },
    });

    await handleFetchConfigMap();
  },
  enabled: computed(
    () => !!authProvider.value?.spec.configMapRef?.name && !!setting.value
  ),
});

const { configMapFormData, formSchema, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  group
);

const handleSaveConfigMap = async () => {
  saving.value = true;
  const configMapToUpdate = convertToSave();
  if (!configMapToUpdate || !authProvider?.value) {
    saving.value = false;
    return;
  }

  await coreApiClient.configMap.updateConfigMap({
    name: authProvider.value.spec.configMapRef?.name as string,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  await handleFetchSettings();
  await handleFetchConfigMap();
  saving.value = false;
};

const displayName = computed(() => {
  if (!authProvider.value) {
    return t("core.common.status.loading");
  }

  return t(
    `core.identity_authentication.fields.display_name.${authProvider.value?.metadata.name}`,
    authProvider.value?.spec.displayName || ""
  );
});

const description = computed(() => {
  if (!authProvider.value) {
    return t("core.common.status.loading");
  }

  return t(
    `core.identity_authentication.fields.description.${authProvider.value?.metadata.name}`,
    authProvider.value?.spec.description || ""
  );
});
</script>

<template>
  <VPageHeader :title="displayName">
    <template #icon>
      <VAvatar
        :src="authProvider?.spec.logo"
        :alt="authProvider?.spec.displayName"
        class="mr-2"
        size="sm"
      />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="bg-white">
        <div v-if="activeTab === 'detail'">
          <VDescription>
            <VDescriptionItem
              :label="
                $t('core.identity_authentication.detail.fields.display_name')
              "
              :content="displayName"
            />
            <VDescriptionItem
              :label="
                $t('core.identity_authentication.detail.fields.description')
              "
              :content="description"
            />
            <VDescriptionItem
              :label="$t('core.identity_authentication.detail.fields.website')"
            >
              <a
                v-if="authProvider?.spec.website"
                :href="authProvider?.spec.website"
                target="_blank"
              >
                {{ authProvider.spec.website }}
              </a>
              <span v-else>
                {{ $t("core.common.text.none") }}
              </span>
            </VDescriptionItem>
            <VDescriptionItem
              :label="
                $t('core.identity_authentication.detail.fields.help_page')
              "
            >
              <a
                v-if="authProvider?.spec.helpPage"
                :href="authProvider?.spec.helpPage"
                target="_blank"
              >
                {{ authProvider.spec.helpPage }}
              </a>
              <span v-else>{{ $t("core.common.text.none") }}</span>
            </VDescriptionItem>
            <VDescriptionItem
              :label="
                $t(
                  'core.identity_authentication.detail.fields.authentication_url'
                )
              "
              :content="authProvider?.spec.authenticationUrl"
            />
          </VDescription>
        </div>
        <div v-if="activeTab === 'setting'" class="bg-white p-4">
          <div>
            <FormKit
              v-if="group && formSchema && configMapFormData"
              :id="group"
              v-model="configMapFormData[group]"
              :name="group"
              :actions="false"
              :preserve="true"
              type="form"
              @submit="handleSaveConfigMap"
            >
              <FormKitSchema
                :schema="toRaw(formSchema)"
                :data="configMapFormData[group]"
              />
            </FormKit>
          </div>
          <div class="pt-5">
            <div class="flex justify-start">
              <VButton
                :loading="saving"
                type="secondary"
                @click="$formkit.submit(group || '')"
              >
                {{ $t("core.common.buttons.save") }}
              </VButton>
            </div>
          </div>
        </div>
      </div>
    </VCard>
  </div>
</template>
