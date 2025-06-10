<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { setFocus } from "@/formkit/utils/focus";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
import type { Policy } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VLoading, VModal, VSpace } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, onMounted, ref, toRaw, toRefs } from "vue";
import { useI18n } from "vue-i18n";

const CONFIG_MAP_GROUP = "default";

const props = withDefaults(
  defineProps<{
    policy?: Policy;
    templateName?: string;
  }>(),
  {
    policy: undefined,
    templateName: undefined,
  }
);

const { policy } = toRefs(props);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const isUpdateMode = computed(() => !!props.policy);

onMounted(async () => {
  setFocus("displayNameInput");
});

const templateName = computed(() => {
  return props.policy?.spec.templateName || props.templateName;
});

const { data: policyTemplate } = useQuery({
  queryKey: ["core:attachment:policy-template", templateName],
  cacheTime: 0,
  queryFn: async () => {
    if (!templateName.value) {
      throw new Error("No template name found");
    }

    const { data } =
      await coreApiClient.storage.policyTemplate.getPolicyTemplate({
        name: templateName.value,
      });
    return data;
  },
  retry: 0,
  enabled: computed(() => !!templateName.value),
});

const { data: setting, isLoading } = useQuery({
  queryKey: ["core:attachment:policy-template:setting", policyTemplate],
  cacheTime: 0,
  queryFn: async () => {
    if (!policyTemplate.value?.spec?.settingName) {
      throw new Error("No setting found");
    }

    const { data } = await coreApiClient.setting.getSetting({
      name: policyTemplate.value.spec.settingName,
    });

    return data;
  },
  retry: 0,
  enabled: computed(() => !!policyTemplate.value?.spec?.settingName),
});

const { data: configMapGroupData } = useQuery({
  queryKey: ["core:attachment:policy-template:configMap", policy],
  cacheTime: 0,
  retry: 0,
  queryFn: async () => {
    if (!policy.value) {
      return {};
    }

    const { data } =
      await consoleApiClient.storage.policy.getPolicyConfigByGroup({
        name: policy.value.metadata.name,
        group: CONFIG_MAP_GROUP,
      });

    return (data || {}) as Record<string, unknown>;
  },
});

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  const { forms } = setting.value.spec;
  return forms.find((item) => item.group === CONFIG_MAP_GROUP)?.formSchema as (
    | FormKitSchemaCondition
    | FormKitSchemaNode
  )[];
});

const isSubmitting = ref(false);

const handleSave = async (data: {
  displayName: string;
  [key: string]: unknown;
}) => {
  console.log(data);
  try {
    isSubmitting.value = true;
    if (isUpdateMode.value) {
      if (!policy.value) {
        throw new Error("No policy found");
      }

      await consoleApiClient.storage.policy.updatePolicyConfigByGroup({
        name: policy.value.metadata.name,
        group: CONFIG_MAP_GROUP,
        body: data,
      });

      await coreApiClient.storage.policy.updatePolicy({
        name: policy.value.metadata.name,
        policy: {
          ...policy.value,
          spec: {
            ...policy.value.spec,
            displayName: data.displayName,
          },
        },
      });
    } else {
      const { data: policies } =
        await coreApiClient.storage.policy.listPolicy();

      const hasDisplayNameDuplicate = policies.items.some(
        (policy) => policy.spec.displayName === data.displayName
      );

      if (hasDisplayNameDuplicate) {
        Toast.error(
          t("core.attachment.policy_editing_modal.toast.policy_name_exists")
        );
        return;
      }

      const { data: newConfigMap } =
        await coreApiClient.configMap.createConfigMap({
          configMap: {
            data: {
              [CONFIG_MAP_GROUP]: JSON.stringify(data),
            },
            apiVersion: "v1alpha1",
            kind: "ConfigMap",
            metadata: {
              generateName: "configMap-",
              name: "",
            },
          },
        });

      await coreApiClient.storage.policy.createPolicy({
        policy: {
          spec: {
            displayName: data.displayName,
            templateName: templateName.value as string,
            configMapName: newConfigMap.metadata.name,
          },
          apiVersion: "storage.halo.run/v1alpha1",
          kind: "Policy",
          metadata: {
            name: "",
            generateName: "attachment-policy-",
          },
        },
      });
    }

    Toast.success(t("core.common.toast.save_success"));
    modal.value?.close();
  } catch (e) {
    console.error("Failed to save attachment policy", e);
  } finally {
    isSubmitting.value = false;
  }
};

const modalTitle = props.policy
  ? t("core.attachment.policy_editing_modal.titles.update", {
      policy: props.policy?.spec.displayName,
    })
  : t("core.attachment.policy_editing_modal.titles.create", {
      policy_template: policyTemplate.value?.spec?.displayName,
    });
</script>
<template>
  <VModal
    ref="modal"
    mount-to-body
    :title="modalTitle"
    :width="600"
    @close="emit('close')"
  >
    <div>
      <VLoading v-if="isLoading" />
      <template v-else>
        <FormKit
          v-if="formSchema && configMapGroupData"
          id="attachment-policy-form"
          :value="toRaw(configMapGroupData) || {}"
          name="attachment-policy-form"
          :preserve="true"
          type="form"
          :config="{ validationVisibility: 'submit' }"
          @submit="handleSave"
        >
          <FormKit
            id="displayNameInput"
            :value="policy?.spec.displayName"
            :label="
              $t(
                'core.attachment.policy_editing_modal.fields.display_name.label'
              )
            "
            type="text"
            name="displayName"
            validation="required|length:0,50"
          ></FormKit>
          <FormKitSchema
            :schema="toRaw(formSchema)"
            :data="toRaw(configMapGroupData) || {}"
          />
        </FormKit>
      </template>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('attachment-policy-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
