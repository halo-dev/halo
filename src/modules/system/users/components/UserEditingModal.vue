<script lang="ts" name="UserEditingModal" setup>
import type { PropType } from "vue";
import { computed, onMounted, ref, watch } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { Role, User } from "@halo-dev/api-client";
import {
  IconCodeBoxLine,
  IconEye,
  VButton,
  VCodemirror,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { v4 as uuid } from "uuid";
import { roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import YAML from "yaml";
import cloneDeep from "lodash.clonedeep";
import { useMagicKeys } from "@vueuse/core";
import { submitForm } from "@formkit/core";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  user: {
    type: Object as PropType<User | null>,
    default: null,
  },
});

const emit = defineEmits(["update:visible", "close"]);

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

const roles = ref<Role[]>([]);
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

const basicRoles = computed(() => {
  return roles.value.filter(
    (role) => role.metadata?.labels?.[roleLabels.TEMPLATE] !== "true"
  );
});

const { Command_Enter } = useMagicKeys();

watch(props, (newVal) => {
  let keyboardWatcher;
  if (newVal.visible) {
    keyboardWatcher = watch(Command_Enter, (v) => {
      if (v) {
        submitForm("user-form");
      }
    });
  } else {
    keyboardWatcher?.unwatch();
  }

  if (newVal.visible && props.user) {
    formState.value.user = cloneDeep(props.user);
    return;
  }
  formState.value = cloneDeep(initialFormState);
});

const handleFetchRoles = async () => {
  try {
    const { data } = await apiClient.extension.role.listv1alpha1Role();
    roles.value = data.items;
  } catch (e) {
    console.error(e);
  }
};

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleCreateUser = async () => {
  try {
    formState.value.saving = true;
    let user: User;

    if (isUpdateMode.value) {
      const response = await apiClient.extension.user.updatev1alpha1User(
        formState.value.user.metadata.name,
        formState.value.user
      );
      user = response.data;
    } else {
      const response = await apiClient.extension.user.createv1alpha1User(
        formState.value.user
      );
      user = response.data;
    }

    if (selectedRole.value) {
      await apiClient.user.grantPermission(user.metadata.name, {
        // @ts-ignore
        roles: [selectedRole.value],
      });
    }

    handleVisibleChange(false);
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

onMounted(handleFetchRoles);
</script>
<template>
  <VModal
    :title="creationModalTitle"
    :visible="visible"
    :width="modalWidth"
    @update:visible="handleVisibleChange"
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
          :options="
          basicRoles.map((role:Role) => {
            return {
              label: role.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] || role.metadata.name,
              value: role.metadata?.name,
            };
          })
        "
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
        <VButton @click="handleVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
