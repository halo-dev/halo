<script lang="ts" setup>
// core libs
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient } from "@/utils/api-client";
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
import YAML from "yaml";
import cloneDeep from "lodash.clonedeep";
import { useMagicKeys } from "@vueuse/core";
import { reset, submitForm } from "@formkit/core";

// constants
import { rbacAnnotations } from "@/constants/annotations";

// hooks
import { useFetchRole } from "@/modules/system/roles/composables/use-role";
import type { FormKitOptionsList } from "@formkit/inputs";
import { setFocus } from "@/formkit/utils/focus";

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

const initialFormState: User = {
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
    name: "",
  },
};

const formState = ref<User>(cloneDeep(initialFormState));
const saving = ref(false);
const rawMode = ref(false);
const raw = ref("");
const selectedRole = ref("");

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const creationModalTitle = computed(() => {
  return isUpdateMode.value ? "编辑用户" : "新增用户";
});

const modalWidth = computed(() => {
  return rawMode.value ? 800 : 700;
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
    if (visible) {
      setFocus(isUpdateMode.value ? "displayNameInput" : "userNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.user,
  (user) => {
    if (user) {
      formState.value = cloneDeep(user);
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
  reset("user-form");
};

const handleCreateUser = async () => {
  try {
    saving.value = true;
    let user: User;

    if (isUpdateMode.value) {
      const response = await apiClient.extension.user.updatev1alpha1User({
        name: formState.value.metadata.name,
        user: formState.value,
      });
      user = response.data;
    } else {
      const response = await apiClient.extension.user.createv1alpha1User({
        user: formState.value,
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
    saving.value = false;
  }
};

const handleRawModeChange = () => {
  rawMode.value = !rawMode.value;

  if (rawMode.value) {
    raw.value = YAML.stringify(formState.value);
  } else {
    formState.value = YAML.parse(raw.value);
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
      <span @click="handleRawModeChange">
        <IconCodeBoxLine v-if="!rawMode" />
        <IconEye v-else />
      </span>
    </template>

    <VCodemirror v-show="rawMode" v-model="raw" height="50vh" language="yaml" />

    <div v-show="!rawMode">
      <FormKit
        id="user-form"
        name="user-form"
        :config="{ validationVisibility: 'submit' }"
        type="form"
        @submit="handleCreateUser"
      >
        <FormKit
          id="userNameInput"
          v-model="formState.metadata.name"
          :disabled="isUpdateMode"
          label="用户名"
          type="text"
          validation="required"
        ></FormKit>
        <FormKit
          id="displayNameInput"
          v-model="formState.spec.displayName"
          label="显示名称"
          type="text"
          validation="required"
        ></FormKit>
        <FormKit
          v-model="formState.spec.email"
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
          v-model="formState.spec.phone"
          label="手机号"
          type="text"
        ></FormKit>
        <FormKit
          v-model="formState.spec.avatar"
          label="头像"
          type="text"
        ></FormKit>
        <FormKit
          v-model="formState.spec.bio"
          label="描述"
          type="textarea"
        ></FormKit>
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          :loading="saving"
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
