<script lang="ts" setup>
import { VTabItem, VTabs } from "@halo-dev/components";
import { ref } from "vue";
import UploadFromUrl from "./UploadFromUrl.vue";

const {
  policyName = undefined,
  groupName = undefined,
  height = undefined,
} = defineProps<{
  policyName?: string;
  groupName?: string;
  height?: string;
}>();

const emit = defineEmits<{
  (event: "done"): void;
}>();

const activeTab = ref("upload");
</script>
<template>
  <VTabs v-model:active-id="activeTab" type="outline">
    <VTabItem
      id="upload"
      :label="$t('core.attachment.upload_modal.upload_options.local_upload')"
    >
      <UppyUpload
        endpoint="/apis/api.console.halo.run/v1alpha1/attachments/upload"
        :disabled="!policyName"
        :meta="{
          policyName: policyName,
          groupName: groupName,
        }"
        width="100%"
        :height="height"
        :allowed-meta-fields="['policyName', 'groupName']"
        :note="
          policyName
            ? ''
            : $t('core.attachment.upload_modal.filters.policy.not_select')
        "
        :done-button-handler="() => emit('done')"
      />
    </VTabItem>
    <VTabItem
      id="download"
      :label="$t('core.attachment.upload_modal.upload_options.download')"
    >
      <UploadFromUrl :policy-name="policyName" :group-name="groupName" />
    </VTabItem>
  </VTabs>
</template>
