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
  <div
    v-if="utils.permission.has(['system:attachments:manage'])"
    v-tooltip="$t('core.attachment.policies_modal.title')"
    class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
    @click="modalVisible = true"
  >
    <IconSettings class="h-4 w-4 text-gray-500 group-hover:text-gray-700" />
  </div>
  <AttachmentPoliciesModal
    v-if="modalVisible"
    :selected-name="selectedName"
    :select-mode="true"
    @select="onSelect"
    @close="onModalClose"
  />
</template>
