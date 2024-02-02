<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { PersonalAccessToken } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    token: PersonalAccessToken;
  }>(),
  {}
);

const queryClient = useQueryClient();
const { t } = useI18n();

function handleDelete() {
  Dialog.warning({
    title: t("core.uc_profile.pat.operations.delete.title"),
    description: t("core.uc_profile.pat.operations.delete.description"),
    async onConfirm() {
      await apiClient.pat.deletePat({
        name: props.token.metadata.name,
      });

      Toast.success(t("core.common.toast.delete_success"));
      queryClient.invalidateQueries({ queryKey: ["personal-access-tokens"] });
    },
  });
}

function handleRevoke() {
  Dialog.warning({
    title: t("core.uc_profile.pat.operations.revoke.title"),
    description: t("core.uc_profile.pat.operations.revoke.description"),
    async onConfirm() {
      await apiClient.pat.revokePat({
        name: props.token.metadata.name,
      });

      Toast.success(t("core.uc_profile.pat.operations.revoke.toast_success"));
      queryClient.invalidateQueries({ queryKey: ["personal-access-tokens"] });
    },
  });
}

async function handleRestore() {
  await apiClient.pat.restorePat({ name: props.token.metadata.name });

  Toast.success(t("core.uc_profile.pat.operations.restore.toast_success"));
  queryClient.invalidateQueries({ queryKey: ["personal-access-tokens"] });
}

const statusText = computed(() => {
  const { expiresAt } = props.token.spec || {};
  if (expiresAt && new Date(expiresAt) < new Date()) {
    return t("core.uc_profile.pat.list.fields.status.expired");
  }
  return t(
    props.token.spec?.revoked
      ? "core.uc_profile.pat.list.fields.status.revoked"
      : "core.uc_profile.pat.list.fields.status.normal"
  );
});

const statusTheme = computed(() => {
  const { expiresAt } = props.token.spec || {};
  if (expiresAt && new Date(expiresAt) < new Date()) {
    return "warning";
  }
  return props.token.spec?.revoked ? "default" : "success";
});
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField
        :title="token.spec?.name || token.metadata.name"
        :description="token.spec?.description"
      ></VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="token.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField v-if="!token.spec?.revoked">
        <template #description>
          <div class="truncate text-xs tabular-nums text-gray-500">
            <span
              v-if="token.spec?.expiresAt"
              v-tooltip="formatDatetime(token.spec.expiresAt)"
            >
              {{
                $t("core.uc_profile.pat.list.fields.expiresAt.dynamic", {
                  expiresAt: relativeTimeTo(token.spec?.expiresAt),
                })
              }}
            </span>
            <span v-else>
              {{ $t("core.uc_profile.pat.list.fields.expiresAt.forever") }}
            </span>
          </div>
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <VStatusDot :text="statusText" :state="statusTheme" />
        </template>
      </VEntityField>
      <VEntityField
        :description="formatDatetime(token.metadata.creationTimestamp)"
      ></VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem
        v-if="!token.spec?.revoked"
        type="danger"
        @click="handleRevoke"
      >
        {{ $t("core.uc_profile.pat.operations.revoke.button") }}
      </VDropdownItem>
      <VDropdownItem v-else @click="handleRestore">
        {{ $t("core.uc_profile.pat.operations.restore.button") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleDelete">
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
