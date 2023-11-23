<script lang="ts" setup>
import { Toast, VButton, VSpace } from "@halo-dev/components";

import { VModal } from "@halo-dev/components";
import { nextTick, onMounted, ref } from "vue";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { useUserStore } from "@/stores/user";
import { useIntervalFn } from "@vueuse/shared";
import { computed } from "vue";

const queryClient = useQueryClient();

const { currentUser, fetchCurrentUser } = useUserStore();

const emit = defineEmits<{
  (event: "close"): void;
}>();

// fixme: refactor VModal component
const shouldRender = ref(false);
const visible = ref(false);

onMounted(() => {
  shouldRender.value = true;
  nextTick(() => {
    visible.value = true;
  });
});

function onClose() {
  visible.value = false;
  setTimeout(() => {
    shouldRender.value = false;
    emit("close");
  }, 200);
}

// count down
const timer = ref(0);
const { pause, resume, isActive } = useIntervalFn(
  () => {
    if (timer.value <= 0) {
      pause();
    } else {
      timer.value--;
    }
  },
  1000,
  {
    immediate: false,
  }
);

const email = ref(currentUser?.spec.email);

const { mutate: sendVerifyCode, isLoading: isSending } = useMutation({
  mutationKey: ["send-verify-code"],
  mutationFn: async () => {
    if (!email.value) {
      Toast.error("请输入电子邮箱");
      throw new Error("email is empty");
    }
    return await apiClient.user.sendEmailVerificationCode({
      emailVerifyRequest: {
        email: email.value,
      },
    });
  },
  onSuccess() {
    Toast.success("验证码已发送");
    timer.value = 60;
    resume();
  },
});

const sendVerifyCodeButtonText = computed(() => {
  if (isSending.value) {
    return "发送中";
  }
  return isActive.value ? `${timer.value} 秒后重发` : "发送验证码";
});

const { mutate: verifyEmail, isLoading: isVerifying } = useMutation({
  mutationKey: ["verify-email"],
  mutationFn: async ({ code }: { code: string }) => {
    return await apiClient.user.verifyEmail({
      verifyCodeRequest: {
        code: code,
      },
    });
  },
  onSuccess() {
    Toast.success("验证成功");
    queryClient.invalidateQueries({ queryKey: ["user-detail"] });
    fetchCurrentUser();
    onClose();
  },
});

function handleVerify(data: { code: string }) {
  verifyEmail({ code: data.code });
}
</script>

<template>
  <VModal
    v-if="shouldRender"
    v-model:visible="visible"
    :title="currentUser?.spec.emailVerified ? '修改电子邮箱' : '绑定电子邮箱'"
    @close="onClose"
  >
    <FormKit
      id="email-verify-form"
      type="form"
      name="email-verify-form"
      @submit="handleVerify"
    >
      <FormKit
        v-model="email"
        type="email"
        :label="currentUser?.spec.emailVerified ? '新电子邮箱' : '电子邮箱'"
        name="email"
        validation="required|email"
      ></FormKit>
      <FormKit type="number" name="code" label="验证码" validation="required">
        <template #suffix>
          <VButton
            :loading="isSending"
            :disabled="isActive"
            class="rounded-none border-y-0 border-l border-r-0 tabular-nums"
            @click="sendVerifyCode"
          >
            {{ sendVerifyCodeButtonText }}
          </VButton>
        </template>
      </FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          :loading="isVerifying"
          type="secondary"
          @click="$formkit.submit('email-verify-form')"
        >
          验证
        </VButton>
        <VButton @click="emit('close')">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
