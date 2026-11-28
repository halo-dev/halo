<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  coreApiClient,
  paginate,
  type Policy,
  type PolicyV1alpha1ApiListPolicyRequest,
} from "@halo-dev/api-client";
import { IconSettings } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import type { PropType } from "vue";
import { computed, defineAsyncComponent, ref, toRefs } from "vue";

const AttachmentPoliciesModal = defineAsyncComponent(
  () =>
    import("@console/modules/contents/attachments/components/AttachmentPoliciesModal.vue")
);

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const { context } = toRefs(props);

const modalVisible = ref(false);

const selectedName = computed(() => context.value._value as string | undefined);
const isDisabled = computed(() => !!context.value.disabled);

const onSelect = (policyName: string) => {
  context.value.node.input(policyName);
};

const onModalClose = async () => {
  modalVisible.value = false;
  const policies = await paginate<PolicyV1alpha1ApiListPolicyRequest, Policy>(
    (params) => coreApiClient.storage.policy.listPolicy(params),
    { size: 1000 }
  );
  if (context.value.node.context) {
    context.value.node.context.attrs.options = policies.map((policy) => ({
      value: policy.metadata.name,
      label: policy.spec.displayName,
    }));
  }
};
</script>

<template>
  <button
    v-if="utils.permission.has(['*'])"
    v-tooltip="$t('core.attachment.policies_modal.title')"
    type="button"
    :aria-label="$t('core.attachment.policies_modal.title')"
    :disabled="isDisabled"
    data-select-suffix
    class="group flex w-10 shrink-0 cursor-pointer select-none items-center justify-center self-stretch border-l transition-all hover:bg-gray-100 focus-visible:bg-gray-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary"
    @click="modalVisible = true"
  >
    <IconSettings
      class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
      aria-hidden="true"
    />
  </button>
  <AttachmentPoliciesModal
    v-if="modalVisible"
    :selected-name="selectedName"
    :select-mode="true"
    @select="onSelect"
    @close="onModalClose"
  />
</template>
