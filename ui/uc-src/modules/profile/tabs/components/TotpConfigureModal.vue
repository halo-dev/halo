<script lang="ts" setup>
import type { TotpRequest } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import { useQRCode } from "@vueuse/integrations/useQRCode";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { data } = useQuery({
  queryKey: ["totp-auth-link"],
  queryFn: async () => {
    const { data } = await ucApiClient.security.twoFactor.getTotpAuthLink();
    return data;
  },
});

const qrcode = useQRCode(computed(() => data.value?.authLink || ""));

const { mutate, isLoading } = useMutation({
  mutationKey: ["configure-totp"],
  mutationFn: async ({ totpRequest }: { totpRequest: TotpRequest }) => {
    await ucApiClient.security.twoFactor.configurerTotp({
      totpRequest: totpRequest,
    });
  },
  onSuccess() {
    Toast.success(t("core.common.toast.save_success"));
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
    :title="$t('core.uc_profile.2fa.methods.totp.operations.configure.title')"
    @close="emit('close')"
  >
    <div>
      <div class="mb-4 space-y-3 border-b border-gray-100 pb-4 text-gray-900">
        <div class="text-sm font-semibold">
          {{
            $t(
              "core.uc_profile.2fa.methods.totp.operations.configure.fields.qrcode.label"
            )
          }}
        </div>
        <img :src="qrcode" class="rounded-base border border-gray-100" />
        <details>
          <summary class="cursor-pointer select-none text-sm text-gray-800">
            {{
              $t(
                "core.uc_profile.2fa.methods.totp.operations.configure.fields.manual.label"
              )
            }}
          </summary>
          <div class="mt-3 rounded-base border border-gray-100 p-2">
            <span class="text-sm text-gray-600">
              {{
                $t(
                  "core.uc_profile.2fa.methods.totp.operations.configure.fields.manual.help"
                )
              }}
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
          :label="
            $t(
              'core.uc_profile.2fa.methods.totp.operations.configure.fields.code.label'
            )
          "
          validation="required"
          :help="
            $t(
              'core.uc_profile.2fa.methods.totp.operations.configure.fields.code.help'
            )
          "
        ></FormKit>
        <FormKit
          type="password"
          :label="
            $t(
              'core.uc_profile.2fa.methods.totp.operations.configure.fields.password.label'
            )
          "
          validation="required"
          name="password"
          :help="
            $t(
              'core.uc_profile.2fa.methods.totp.operations.configure.fields.password.help'
            )
          "
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
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
