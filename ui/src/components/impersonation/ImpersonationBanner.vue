<script lang="ts" setup>
import { VAlert, VButton } from "@halo-dev/components";
import { stores } from "@halo-dev/ui-shared";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const { currentUser } = storeToRefs(stores.currentUser());

function handleExit() {
  window.location.href = "/logout/impersonate";
}
</script>

<template>
  <VAlert
    v-if="currentUser?.impersonator"
    class="m-4"
    type="warning"
    :closable="false"
    :title="
      t('core.impersonation.banner.text', {
        displayName: currentUser.user.spec.displayName,
        impersonator: currentUser.impersonator,
      })
    "
  >
    <template #actions>
      <VButton size="sm" @click="handleExit">
        {{ t("core.impersonation.operations.exit.title") }}
      </VButton>
    </template>
  </VAlert>
</template>
