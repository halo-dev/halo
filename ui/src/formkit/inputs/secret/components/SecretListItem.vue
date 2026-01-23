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
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { Q_KEY } from "../composables/use-secrets-fetch";
import type { RequiredKey } from "../types";
import SecretEditModal from "./SecretEditModal.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    secret: Secret;
    selected?: boolean;
    requiredKeys?: RequiredKey[];
  }>(),
  {
    selected: false,
    requiredKeys: () => [],
  }
);

const emit = defineEmits<{
  (event: "click"): void;
}>();

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

const description = computed(() => {
  return (
    props.secret.metadata.annotations?.[secretAnnotations.DESCRIPTION] || ""
  );
});

const keys = computed(() => {
  return Object.keys(props.secret.stringData || {});
});

const descriptionText = computed(() => {
  if (keys.value.length > 0) {
    return t("core.formkit.secret.includes_keys", {
      keys: keys.value.join(", "),
    });
  }
  return t("core.formkit.secret.no_fields");
});
</script>

<template>
  <SecretEditModal
    v-if="editModalVisible"
    :secret="secret"
    :required-keys="requiredKeys"
    @close="editModalVisible = false"
  />
  <VEntity :is-selected="selected" @click="emit('click')">
    <template #checkbox>
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField
        :title="description || secret.metadata.name"
        :description="descriptionText"
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
