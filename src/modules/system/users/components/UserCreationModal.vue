<script lang="ts" name="UserCreationModal" setup>
import type { PropType } from "vue";
import { computed, ref, watch } from "vue";
import { axiosInstance } from "@halo-dev/admin-shared";
import { IconSave, VButton, VModal } from "@halo-dev/components";
import type { User } from "@/types/extension";
import { v4 as uuid } from "uuid";

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

interface creationFormState {
  user: User;
  saving: boolean;
}

const creationForm = ref<creationFormState>({
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
});

const isUpdateMode = computed(() => {
  return !!creationForm.value.user.metadata.creationTimestamp;
});

const creationModalTitle = computed(() => {
  return isUpdateMode.value ? "编辑用户" : "新增用户";
});

watch(props, (newVal) => {
  if (newVal.visible && props.user) {
    creationForm.value.user = props.user;
  }
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleCreateUser = async () => {
  try {
    creationForm.value.saving = true;

    if (isUpdateMode.value) {
      await axiosInstance.put(
        `/api/v1alpha1/users/${creationForm.value.user.metadata.name}`,
        creationForm.value.user
      );
    } else {
      await axiosInstance.post("/api/v1alpha1/users", creationForm.value.user);
    }

    handleVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    creationForm.value.saving = false;
  }
};
</script>
<template>
  <VModal
    :title="creationModalTitle"
    :visible="visible"
    :width="700"
    @update:visible="handleVisibleChange"
  >
    <FormKit
      id="user-form"
      :actions="false"
      type="form"
      @submit="handleCreateUser"
    >
      <FormKit
        v-model="creationForm.user.metadata.name"
        label="用户名"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="creationForm.user.spec.displayName"
        label="显示名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="creationForm.user.spec.email"
        label="电子邮箱"
        type="email"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="creationForm.user.spec.phone"
        label="手机号"
        type="text"
      ></FormKit>
      <FormKit
        v-model="creationForm.user.spec.avatar"
        label="头像"
        type="text"
      ></FormKit>
      <FormKit
        v-model="creationForm.user.spec.bio"
        label="描述"
        type="textarea"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VButton
        :loading="creationForm.saving"
        type="secondary"
        @click="$formkit.submit('user-form')"
      >
        <template #icon>
          <IconSave class="h-full w-full" />
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>
