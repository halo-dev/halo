<script lang="ts" setup>
// core libs
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { User } from "@halo-dev/api-client";

// components
import {
  IconCodeBoxLine,
  IconEye,
  VButton,
  VCodemirror,
  VModal,
  VSpace,
} from "@halo-dev/components";

// libs
import { v4 as uuid } from "uuid";
import YAML from "yaml";
import cloneDeep from "lodash.clonedeep";
import { useMagicKeys } from "@vueuse/core";
import { reset, submitForm } from "@formkit/core";

// constants
import { rbacAnnotations } from "@/constants/annotations";

// hooks
import { useFetchRole } from "@/modules/system/roles/composables/use-role";
import type { FormKitOptionsList } from "@formkit/inputs";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    user: User | null;
  }>(),
  {
    visible: false,
    user: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

interface FormState {
  user: User;
  saving: boolean;
  rawMode: boolean;
  raw: string;
}

const initialFormState: FormState = {
  user: {
    spec: {
      displayName: "",
      avatar: "",
      email: "",
      phone: "",
      password: "",
      bio: "",
      disabled: false,
      loginHistoryLimit: 0,
    },
    apiVersion: "v1alpha1",
    kind: "User",
    metadata: {
      name: uuid(),
    },
  },
  saving: false,
  rawMode: false,
  raw: "",
};

const formState = ref<FormState>(cloneDeep(initialFormState));
const selectedRole = ref("");

const isUpdateMode = computed(() => {
  return !!formState.value.user.metadata.creationTimestamp;
});

const creationModalTitle = computed(() => {
  return isUpdateMode.value ? "编辑用户" : "新增用户";
});

const modalWidth = computed(() => {
  return formState.value.rawMode ? 800 : 700;
});

const { roles } = useFetchRole();
const rolesMap = computed<FormKitOptionsList>(() => {
  return roles.value.map((role) => {
    return {
      label:
        role.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
        role.metadata.name,
      value: role.metadata?.name,
    };
  });
});

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("user-form");
  }
});

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      handleResetForm();
    }
  }
);

watch(
  () => props.user,
  (user) => {
    if (user) {
      formState.value.user = cloneDeep(user);
    } else {
      handleResetForm();
    }
  }
);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  formState.value.user.metadata.name = uuid();
  reset("user-form");
};

const handleCreateUser = async () => {
  try {
    formState.value.saving = true;
    let user: User;

    if (isUpdateMode.value) {
      const response = await apiClient.extension.user.updatev1alpha1User({
        name: formState.value.user.metadata.name,
        user: formState.value.user,
      });
      user = response.data;
    } else {
      const response = await apiClient.extension.user.createv1alpha1User({
        user: formState.value.user,
      });
      user = response.data;
    }

    if (selectedRole.value) {
      await apiClient.user.grantPermission({
        name: user.metadata.name,
        grantRequest: {
          roles: [selectedRole.value],
        },
      });
    }

    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    formState.value.saving = false;
  }
};

const handleRawModeChange = () => {
  formState.value.rawMode = !formState.value.rawMode;

  if (formState.value.rawMode) {
    formState.value.raw = YAML.stringify(formState.value.user);
  } else {
    formState.value.user = YAML.parse(formState.value.raw);
  }
};
</script>
<template>
  <VModal
    :title="creationModalTitle"
    :visible="visible"
    :width="modalWidth"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <div class="modal-header-action" @click="handleRawModeChange">
        <IconCodeBoxLine v-if="!formState.rawMode" />
        <IconEye v-else />
      </div>
    </template>

    <VCodemirror
      v-show="formState.rawMode"
      v-model="formState.raw"
      height="50vh"
      language="yaml"
    />

    <div v-show="!formState.rawMode">
      <FormKit id="user-form" type="form" @submit="handleCreateUser">
        <FormKit
          v-model="formState.user.metadata.name"
          :disabled="isUpdateMode"
          label="用户名"
          type="text"
          validation="required"
        ></FormKit>
        <FormKit
          v-model="formState.user.spec.displayName"
          label="显示名称"
          type="text"
          validation="required"
        ></FormKit>
        <FormKit
          v-model="formState.user.spec.email"
          label="电子邮箱"
          type="email"
          validation="required"
        ></FormKit>
        <FormKit
          v-model="selectedRole"
          :options="rolesMap"
          label="角色"
          type="select"
        ></FormKit>
        <FormKit
          v-model="formState.user.spec.phone"
          label="手机号"
          type="text"
        ></FormKit>
        <FormKit
          v-model="formState.user.spec.avatar"
          label="头像"
          type="text"
        ></FormKit>
        <FormKit
          v-model="formState.user.spec.bio"
          label="描述"
          type="textarea"
        ></FormKit>
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          :loading="formState.saving"
          type="secondary"
          @click="$formkit.submit('user-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
