<script lang="ts" setup>
import type { Policy } from "@halo-dev/api-client";
import { IconCheckboxCircle } from "@halo-dev/components";
import { computed } from "vue";
import { useFetchAttachmentPolicyTemplate } from "../composables/use-attachment-policy";

const props = withDefaults(
  defineProps<{
    policy?: Policy;
    isSelected?: boolean;
    features?: { checkIcon?: boolean };
  }>(),
  {
    policy: undefined,
    isSelected: false,
    features: () => {
      return {
        checkIcon: false,
      };
    },
  }
);

const { policyTemplates } = useFetchAttachmentPolicyTemplate();

const policyTemplate = computed(() => {
  return policyTemplates.value?.find(
    (template) => template.metadata.name === props.policy?.spec.templateName
  );
});
</script>

<template>
  <button
    type="button"
    class="inline-flex h-full w-full items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-2.5 text-sm font-medium text-gray-800 hover:bg-gray-50 hover:shadow-sm"
    :class="{ '!bg-gray-100 shadow-sm': isSelected }"
  >
    <div class="inline-flex w-full flex-1 flex-col space-y-0.5 text-left">
      <slot name="text">
        <div>
          {{ policy?.spec.displayName }}
        </div>
        <div class="text-xs font-normal text-gray-600">
          {{ policyTemplate?.spec?.displayName || "--" }}
        </div>
      </slot>
    </div>

    <div class="flex-none">
      <IconCheckboxCircle
        v-if="isSelected && features.checkIcon"
        class="text-primary"
      />
      <slot name="actions" />
    </div>
  </button>
</template>
