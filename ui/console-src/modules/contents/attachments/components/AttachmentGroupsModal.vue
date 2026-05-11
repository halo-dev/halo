<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VEmpty,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { onMounted, ref, toRaw } from "vue";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import AttachmentGroupBadge from "./AttachmentGroupBadge.vue";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

const props = withDefaults(
  defineProps<{
    selectedName?: string;
  }>(),
  {
    selectedName: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "select", groupName: string): void;
}>();

const { groups, isLoading, handleFetchGroups } = useFetchAttachmentGroup();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const creationModalVisible = ref(false);

const onCreationModalClose = () => {
  creationModalVisible.value = false;
  handleFetchGroups();
};

const currentSelectedName = ref();

onMounted(() => {
  currentSelectedName.value = toRaw(props.selectedName);
});

function handleConfirm() {
  emit("select", currentSelectedName.value);
  modal.value?.close();
}
</script>
<template>
  <VModal
    ref="modal"
    :width="750"
    :title="$t('core.attachment.groups_modal.title')"
    :body-class="['!p-0']"
    :layer-closable="true"
    :centered="false"
    :mount-to-body="true"
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
    <VEmpty
      v-if="!groups?.length && !isLoading"
      :message="$t('core.attachment.groups_modal.empty.message')"
      :title="$t('core.attachment.groups_modal.empty.title')"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchGroups">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
          <VButton type="secondary" @click="creationModalVisible = true">
            <template #icon>
              <IconAddCircle />
            </template>
            {{ $t("core.common.buttons.new") }}
          </VButton>
        </VSpace>
      </template>
    </VEmpty>
    <div v-else class="p-4">
      <div
        class="grid grid-cols-2 gap-x-2 gap-y-3 md:grid-cols-3 lg:grid-cols-4"
      >
        <AttachmentGroupBadge
          v-for="group in groups"
          :key="group.metadata.name"
          :group="group"
          :is-selected="group.metadata.name === currentSelectedName"
          :features="{ actions: false, checkIcon: true }"
          @click="currentSelectedName = group.metadata.name"
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="handleConfirm">
          {{ $t("core.common.buttons.confirm") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>

  <AttachmentGroupEditingModal
    v-if="creationModalVisible"
    @close="onCreationModalClose"
  />
</template>
