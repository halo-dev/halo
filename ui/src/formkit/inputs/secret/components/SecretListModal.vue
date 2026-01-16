<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VEntityContainer,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { ref } from "vue";
import { useSecretsFetch } from "../composables/use-secrets-fetch";
import type { RequiredKey } from "../types";
import SecretCreationModal from "./SecretCreationModal.vue";
import SecretListItem from "./SecretListItem.vue";

const props = defineProps<{
  selectedSecretName: string;
  requiredKeys?: RequiredKey[];
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "select", secretName: string): void;
}>();

const { data } = useSecretsFetch();

const creationModalVisible = ref(false);

const selectedSecretName = ref(props.selectedSecretName);

function handleSelect() {
  emit("select", selectedSecretName.value);
  modal?.value?.close();
}
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :title="$t('core.formkit.secret.list_modal.title')"
    :width="650"
    mount-to-body
    layer-closable
    :centered="false"
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
        :selected="selectedSecretName === secret.metadata.name"
        :required-keys="requiredKeys"
        @click="selectedSecretName = secret.metadata.name"
      >
        <template #checkbox>
          <input
            v-model="selectedSecretName"
            type="radio"
            name="secret"
            :value="secret.metadata.name"
          />
        </template>
      </SecretListItem>
    </VEntityContainer>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="handleSelect">
          {{ $t("core.common.buttons.confirm") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>

  <SecretCreationModal
    v-if="creationModalVisible"
    :required-keys="requiredKeys"
    @close="creationModalVisible = false"
  />
</template>
