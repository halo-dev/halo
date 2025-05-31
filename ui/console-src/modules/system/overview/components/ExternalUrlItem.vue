<script setup lang="ts">
import { useGlobalInfoFetch } from "@console/composables/use-global-info";
import {
  IconRiPencilFill,
  VAlert,
  VDescriptionItem,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import ExternalUrlForm from "./ExternalUrlForm.vue";

const { globalInfo } = useGlobalInfoFetch();

const isExternalUrlValid = computed(() => {
  if (!globalInfo.value) {
    return true;
  }

  if (!globalInfo.value?.externalUrl) {
    return false;
  }

  const url = new URL(globalInfo.value.externalUrl);
  const { host: currentHost, protocol: currentProtocol } = window.location;
  return url.host === currentHost && url.protocol === currentProtocol;
});

const showExternalUrlForm = ref(false);
</script>

<template>
  <VDescriptionItem :label="$t('core.overview.fields.external_url')">
    <div v-if="!showExternalUrlForm" class="flex items-center gap-3">
      <span v-if="globalInfo?.externalUrl">
        {{ globalInfo?.externalUrl }}
      </span>
      <span v-else>
        {{ $t("core.overview.fields_values.external_url.not_setup") }}
      </span>
      <IconRiPencilFill
        class="cursor-pointer text-sm text-gray-600 hover:text-gray-900"
        @click="showExternalUrlForm = true"
      />
    </div>
    <ExternalUrlForm v-else @close="showExternalUrlForm = false" />
    <VAlert
      v-if="!isExternalUrlValid && !showExternalUrlForm"
      class="mt-3"
      type="warning"
      :title="$t('core.common.text.warning')"
      :closable="false"
    >
      <template #description>
        {{ $t("core.overview.alert.external_url_invalid") }}
      </template>
    </VAlert>
  </VDescriptionItem>
</template>
