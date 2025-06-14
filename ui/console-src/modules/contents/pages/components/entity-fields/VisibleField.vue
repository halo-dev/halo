<script lang="ts" setup>
import type { ListedSinglePage, SinglePage } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { IconEye, IconEyeOff, Toast, VEntityField } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);

const { mutate: changeVisibleMutation } = useMutation({
  mutationFn: async (singlePage: SinglePage) => {
    return await coreApiClient.content.singlePage.patchSinglePage({
      name: singlePage.metadata.name,
      jsonPatchInner: [
        {
          op: "add",
          path: "/spec/visible",
          value: singlePage.spec.visible === "PRIVATE" ? "PUBLIC" : "PRIVATE",
        },
      ],
    });
  },
  retry: 3,
  onSuccess: () => {
    Toast.success(t("core.common.toast.operation_success"));
    queryClient.invalidateQueries({ queryKey: ["singlePages"] });
  },
  onError: () => {
    Toast.error(t("core.common.toast.operation_failed"));
  },
});
</script>

<template>
  <VEntityField>
    <template #description>
      <IconEye
        v-if="singlePage.page.spec.visible === 'PUBLIC'"
        v-tooltip="$t('core.page.filters.visible.items.public')"
        class="cursor-pointer text-sm transition-all hover:text-blue-600"
        @click="changeVisibleMutation(singlePage.page)"
      />
      <IconEyeOff
        v-if="singlePage.page.spec.visible === 'PRIVATE'"
        v-tooltip="$t('core.page.filters.visible.items.private')"
        class="cursor-pointer text-sm transition-all hover:text-blue-600"
        @click="changeVisibleMutation(singlePage.page)"
      />
    </template>
  </VEntityField>
</template>
