<script lang="ts" setup>
import { reset } from "@formkit/core";
import type { AuthProvider } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { VModal } from "@halo-dev/components";
import { computed, ref, toRaw, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    authProvider?: AuthProvider;
  }>(),
  {
    visible: false,
    authProvider: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: AuthProvider = {
  apiVersion: "v1alpha1",
  kind: "AuthProvider",
  metadata: {
    name: "",
    generateName: "auth-provider-",
  },
  spec: {
    authenticationUrl: "",
    configMapKeyRef: {
      key: "",
      name: "",
    },
    description: "",
    displayName: "",
    enabled: false,
    helpPage: "",
    logo: "",
    settingRef: {
      group: "",
      name: "",
    },
    website: "",
  },
};

const formState = ref<AuthProvider>(cloneDeep(initialFormState));

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑认证方式" : "新增认证方式";
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("auth-provider-form");
};

watch(
  () => props.authProvider,
  (authProvider) => {
    if (authProvider) {
      formState.value = toRaw(authProvider);
    } else {
      handleResetForm();
    }
  }
);
</script>

<template>
  <VModal
    :title="modalTitle"
    :visible="visible"
    :width="700"
    @update:visible="onVisibleChange"
  >
    // TODO
  </VModal>
</template>
