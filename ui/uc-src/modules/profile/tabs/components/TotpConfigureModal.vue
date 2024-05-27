<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import { useQRCode } from "@vueuse/integrations/useQRCode";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { computed } from "vue";
import type { TotpRequest } from "@halo-dev/api-client";
import { ref } from "vue";

const queryClient = useQueryClient();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { data } = useQuery({
  queryKey: ["totp-auth-link"],
  queryFn: async () => {
    const { data } = await apiClient.twoFactor.getTotpAuthLink();
    return data;
  },
});

const qrcode = useQRCode(computed(() => data.value?.authLink || ""));

const { mutate, isLoading } = useMutation({
  mutationKey: ["configure-totp"],
  mutationFn: async ({ totpRequest }: { totpRequest: TotpRequest }) => {
    await apiClient.twoFactor.configurerTotp({
      totpRequest: totpRequest,
    });
  },
  onSuccess() {
    Toast.success("配置成功");
    queryClient.invalidateQueries({ queryKey: ["two-factor-settings"] });
    modal.value?.close();
  },
});

function onSubmit(data: TotpRequest) {
  mutate({ totpRequest: data });
}
</script>

<template>
  <VModal
    ref="modal"
    :width="500"
    :centered="false"
    title="TOTP 配置"
    @close="emit('close')"
  >
    <div>
      <div class="mb-4 space-y-3 border-b border-gray-100 pb-4 text-gray-900">
        <div class="text-sm font-semibold">使用验证器应用扫描下方二维码：</div>
        <img :src="qrcode" class="rounded-base border border-gray-100" />
        <details>
          <summary class="cursor-pointer select-none text-sm text-gray-800">
            如果无法扫描二维码，点击查看代替步骤
          </summary>
          <div class="mt-3 rounded-base border border-gray-100 p-2">
            <span class="text-sm text-gray-600">
              使用以下代码手动配置验证器应用：
            </span>
            <div class="mt-2">
              <code
                class="select-all rounded bg-gray-200 p-1 text-xs text-gray-900"
              >
                {{ data?.rawSecret }}
              </code>
            </div>
          </div>
        </details>
      </div>
      <FormKit id="totp-form" type="form" name="totp-form" @submit="onSubmit">
        <FormKit
          type="number"
          name="code"
          label="验证码"
          validation="required"
          help="从验证器应用获得的 6 位验证码"
        ></FormKit>
        <FormKit
          type="password"
          label="验证密码"
          validation="required"
          name="password"
          help="当前账号的登录密码"
          autocomplete="current-password"
        ></FormKit>
        <FormKit
          :model-value="data?.rawSecret"
          type="hidden"
          name="secret"
        ></FormKit>
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="secondary"
          @click="$formkit.submit('totp-form')"
        >
          完成
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
