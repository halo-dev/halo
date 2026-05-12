<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  coreApiClient,
  paginate,
  type Group,
  type GroupV1alpha1ApiListGroupRequest,
} from "@halo-dev/api-client";
import { IconSettings } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import type { PropType } from "vue";
import { computed, defineAsyncComponent, ref, toRefs } from "vue";

const AttachmentGroupsModal = defineAsyncComponent(
  () =>
    import("@console/modules/contents/attachments/components/AttachmentGroupsModal.vue")
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

const onSelect = (groupName: string) => {
  context.value.node.input(groupName);
};

const onModalClose = async () => {
  modalVisible.value = false;
  const groups = await paginate<GroupV1alpha1ApiListGroupRequest, Group>(
    (params) => coreApiClient.storage.group.listGroup(params),
    {
      labelSelector: ["!halo.run/hidden"],
      sort: ["metadata.creationTimestamp,desc"],
      size: 1000,
    }
  );
  if (context.value.node.context) {
    context.value.node.context.attrs.options = groups.map((group) => ({
      value: group.metadata.name,
      label: group.spec.displayName,
    }));
  }
};
</script>

<template>
  <div
    v-if="utils.permission.has(['system:attachments:manage'])"
    v-tooltip="$t('core.attachment.groups_modal.title')"
    class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
    @click="modalVisible = true"
  >
    <IconSettings class="h-4 w-4 text-gray-500 group-hover:text-gray-700" />
  </div>
  <AttachmentGroupsModal
    v-if="modalVisible"
    :selected-name="selectedName"
    @select="onSelect"
    @close="onModalClose"
  />
</template>
