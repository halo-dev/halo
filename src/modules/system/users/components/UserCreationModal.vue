<script lang="ts" name="UserCreationModal" setup>
import type { PropType } from "vue";
import { computed, ref, watch } from "vue";
import { axiosInstance } from "@halo-dev/admin-shared";
import {
  IconSave,
  VButton,
  VInput,
  VModal,
  VTextarea,
} from "@halo-dev/components";
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
    <form>
      <div class="space-y-6 divide-y-0 sm:divide-y sm:divide-gray-200">
        <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            用户名
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VInput v-model="creationForm.user.metadata.name"></VInput>
          </div>
        </div>

        <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            昵称
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VInput v-model="creationForm.user.spec.displayName"></VInput>
          </div>
        </div>

        <div class="sm:grid sm:grid-cols-3 sm:items-center sm:gap-4 sm:pt-5">
          <label class="block text-sm font-medium text-gray-700">
            电子邮箱
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VInput v-model="creationForm.user.spec.email"></VInput>
          </div>
        </div>

        <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            手机号
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VInput v-model="creationForm.user.spec.phone"></VInput>
          </div>
        </div>
        <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            头像
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VInput v-model="creationForm.user.spec.avatar"></VInput>
          </div>
        </div>
        <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            描述
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <VTextarea v-model="creationForm.user.spec.bio"></VTextarea>
          </div>
        </div>
      </div>
    </form>
    <template #footer>
      <VButton
        :loading="creationForm.saving"
        type="secondary"
        @click="handleCreateUser"
      >
        <template #icon>
          <IconSave class="h-full w-full" />
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>
