<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VEntityContainer,
  VModal,
} from "@halo-dev/components";
import { ref } from "vue";
import { useSecretsFetch } from "../composables/use-secrets-fetch";
import SecretCreationModal from "./SecretCreationModal.vue";
import SecretListItem from "./SecretListItem.vue";

const modal = ref<InstanceType<typeof VModal> | null>(null);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { data } = useSecretsFetch();

const creationModalVisible = ref(false);
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :title="$t('core.formkit.secret.list_modal.title')"
    :width="650"
    @close="emit('close')"
  >
    <template #actions>
      <span
        v-tooltip="$t('core.common.buttons.new')"
        @click="creationModalVisible = true"
      >
        <IconAddCircle />
      </span>
    </template>
    <VEntityContainer>
      <SecretListItem
        v-for="secret in data?.items"
        :key="secret.metadata.name"
        :secret="secret"
      />
    </VEntityContainer>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>

  <SecretCreationModal
    v-if="creationModalVisible"
    @close="creationModalVisible = false"
  />
</template>
