<script setup lang="ts">
import { setFocus } from "@/formkit/utils/focus";
import { coreApiClient, type Attachment } from "@halo-dev/api-client";
import { Toast, VButton, VSpace } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(defineProps<{ attachment: Attachment }>(), {});

const emit = defineEmits<{
  (event: "close"): void;
}>();

onMounted(() => {
  setFocus("displayName");
});

const isSubmitting = ref(false);

async function onSubmit({ displayName }: { displayName: string }) {
  try {
    isSubmitting.value = true;

    await coreApiClient.storage.attachment.patchAttachment({
      name: props.attachment.metadata.name,
      jsonPatchInner: [
        {
          op: "add",
          path: "/spec/displayName",
          value: displayName,
        },
      ],
    });

    Toast.success(t("core.common.toast.save_success"));

    queryClient.invalidateQueries({
      queryKey: ["core:attachment-by-name", props.attachment.metadata.name],
    });

    emit("close");
  } catch (error) {
    console.error("Failed to update displayName", error);
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  } finally {
    isSubmitting.value = false;
  }
}
</script>
<template>
  <FormKit
    id="attachment-display-name-form"
    type="form"
    name="attachment-display-name-form"
    @submit="onSubmit"
  >
    <FormKit
      id="displayName"
      :model-value="attachment.spec.displayName"
      type="text"
      name="displayName"
      validation="required:trim"
      :classes="{ outer: '!pb-0' }"
    ></FormKit>
  </FormKit>
  <VSpace class="mt-4">
    <VButton
      type="secondary"
      @click="$formkit.submit('attachment-display-name-form')"
    >
      {{ $t("core.common.buttons.save") }}
    </VButton>
    <VButton @click="emit('close')">
      {{ $t("core.common.buttons.cancel") }}
    </VButton>
  </VSpace>
</template>
