<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import { reset } from "@formkit/core";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    policyName?: string;
    groupName?: string;
  }>(),
  {
    policyName: undefined,
    groupName: undefined,
  }
);

onMounted(() => {
  setFocus("url");
});

const downloading = ref(false);

async function onSubmit(data: { url: string }) {
  try {
    downloading.value = true;

    if (!props.policyName) {
      throw new Error("Policy name is required");
    }

    await consoleApiClient.storage.attachment.externalTransferAttachment({
      uploadFromUrlRequest: {
        url: data.url,
        policyName: props.policyName,
        groupName: props.groupName,
      },
    });

    Toast.success(
      t("core.attachment.upload_modal.download_form.toast.success")
    );

    reset("url");
  } catch (error) {
    return error;
  } finally {
    downloading.value = false;
  }
}
</script>
<template>
  <FormKit
    id="upload-from-url"
    type="form"
    name="upload-from-url"
    :config="{ validationVisibility: 'submit' }"
    @submit="onSubmit"
  >
    <FormKit
      id="url"
      type="url"
      name="url"
      :label="$t('core.attachment.upload_modal.download_form.fields.url.label')"
      :validation="[['required'], ['url']]"
    />
  </FormKit>

  <div class="mt-4">
    <VButton
      type="secondary"
      :loading="downloading"
      @click="$formkit.submit('upload-from-url')"
    >
      {{ $t("core.common.buttons.download") }}
    </VButton>
  </div>
</template>
