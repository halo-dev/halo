<script lang="ts" setup>
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
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
import { computed, ref, shallowRef, toRaw, toRefs, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";

const { t } = useI18n();
const route = useRoute();
const { name } = toRefs(route.params);

const tabs = shallowRef<{ id: string; label: string }[]>([
  {
    id: "detail",
    label: t("core.identity_authentication.tabs.detail"),
  },
]);

const activeTab = ref<string>("detail");

const { data: authProvider } = useQuery<AuthProvider>({
  queryKey: ["auth-provider", name],
  queryFn: async () => {
    const { data } = await coreApiClient.auth.authProvider.getAuthProvider({
      name: name.value as string,
    });
    return data;
  },
  enabled: computed(() => !!name.value),
});

watch(
  () => authProvider.value,
  () => {
    if (authProvider.value?.spec.settingRef?.name) {
      tabs.value = [
        ...tabs.value,
        {
          id: "setting",
          label: t("core.identity_authentication.tabs.setting"),
        },
      ];
    }
  },
  {
    immediate: true,
  }
);

// setting
const isSubmitting = ref(false);
const settingName = computed(() => authProvider.value?.spec.settingRef?.name);
const settingGroup = computed(
  () => authProvider.value?.spec.settingRef?.group as string
);
const configMapName = computed(
  () => authProvider.value?.spec.configMapRef?.name
);

const { data: setting, refetch: refetchSettings } = useQuery<Setting>({
  queryKey: ["auth-provider-setting", settingName.value],
  queryFn: async () => {
    const { data } = await coreApiClient.setting.getSetting(
      {
        name: settingName.value as string,
      },
      {
        mute: true,
      }
    );
    return data;
  },
  enabled: computed(() => !!settingName.value),
});

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  const { forms } = setting.value.spec;
  return forms.find((item) => item.group === settingGroup.value)
    ?.formSchema as (FormKitSchemaCondition | FormKitSchemaNode)[];
});

const { data: configMap, refetch: refetchConfigMap } = useQuery({
  queryKey: ["auth-provider-configMap", configMapName],
  queryFn: async () => {
    const { data } = await coreApiClient.configMap.getConfigMap(
      {
        name: configMapName.value as string,
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
    data[settingGroup.value] = "{}";
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

    await refetchConfigMap();
  },
  enabled: computed(() => !!configMapName.value && !!setting.value),
});

const configMapData = computed(() => {
  if (!configMap.value) {
    return {};
  }
  return JSON.parse(configMap.value.data?.[settingGroup.value] || "{}");
});

const onSubmit = async (data: Record<string, unknown>) => {
  try {
    isSubmitting.value = true;

    const { data: configMapToUpdate } =
      await coreApiClient.configMap.getConfigMap({
        name: configMapName.value as string,
      });

    await coreApiClient.configMap.updateConfigMap({
      name: configMapName.value as string,
      configMap: {
        ...configMapToUpdate,
        data: {
          ...(configMapToUpdate?.data || {}),
          [settingGroup.value]: JSON.stringify(data),
        },
      },
    });
    await refetchSettings();
    await refetchConfigMap();
    Toast.success(t("core.common.toast.save_success"));
  } catch (error) {
    console.error(error);
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  } finally {
    isSubmitting.value = false;
  }
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
              v-if="settingGroup && formSchema && configMapData"
              :id="settingGroup"
              :model-value="configMapData"
              :name="settingGroup"
              :preserve="true"
              type="form"
              @submit="onSubmit"
            >
              <FormKitSchema
                :schema="toRaw(formSchema)"
                :data="configMapData"
              />
            </FormKit>
          </div>
          <div class="pt-5">
            <div class="flex justify-start">
              <VButton
                :loading="isSubmitting"
                type="secondary"
                @click="$formkit.submit(settingGroup || '')"
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
