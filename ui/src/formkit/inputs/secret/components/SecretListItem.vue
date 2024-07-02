<script lang="ts" setup>
import { secretAnnotations } from "@/constants/annotations";
import { coreApiClient, type Secret } from "@halo-dev/api-client";
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
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Q_KEY } from "../composables/use-secrets-fetch";
import SecretEditModal from "./SecretEditModal.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    secret: Secret;
  }>(),
  {}
);

function handleDelete() {
  Dialog.warning({
    title: t("core.formkit.secret.operations.delete.title"),
    description: t("core.formkit.secret.operations.delete.description"),
    confirmType: "danger",
    async onConfirm() {
      await coreApiClient.secret.deleteSecret({
        name: props.secret.metadata.name,
      });

      Toast.success(t("core.common.toast.delete_success"));
      queryClient.invalidateQueries({ queryKey: Q_KEY() });
    },
  });
}

const editModalVisible = ref(false);
</script>

<template>
  <SecretEditModal
    v-if="editModalVisible"
    :secret="secret"
    @close="editModalVisible = false"
  />
  <VEntity>
    <template #start>
      <VEntityField
        :title="secret.metadata.name"
        :description="
          secret.metadata.annotations?.[secretAnnotations.DESCRIPTION]
        "
      ></VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="secret.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="editModalVisible = true">
        {{ $t("core.common.buttons.edit") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleDelete">
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
