<script lang="ts" setup>
import { VButton, VModal } from "@halo-dev/components";
import { ref } from "vue";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="920"
    height="calc(100vh - 20px)"
    :title="$t('core.uc_attachment.upload_modal.title')"
    mount-to-body
    @close="emit('close')"
  >
    <div class="w-full p-4">
      <UppyUpload
        endpoint="/apis/uc.api.storage.halo.run/v1alpha1/attachments/-/upload"
        width="100%"
        :done-button-handler="() => modal?.close()"
      />
    </div>

    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
