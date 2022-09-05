<script lang="ts" setup>
// core libs
import { onMounted, ref } from "vue";

// components
import { IconAddCircle, IconMore, VButton, VSpace } from "@halo-dev/components";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

// types
import type { Group } from "@halo-dev/api-client";

import { useRouteQuery } from "@vueuse/router";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";

const props = withDefaults(
  defineProps<{
    selectedGroup: Group | undefined;
    readonly?: boolean;
  }>(),
  {
    selectedGroup: undefined,
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "update:selectedGroup", group: Group): void;
  (event: "select", group: Group): void;
  (event: "update"): void;
}>();

const defaultGroups: Group[] = [
  {
    spec: {
      displayName: "全部",
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "",
    },
  },
  {
    spec: {
      displayName: "未分组",
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "none",
    },
  },
];

const { groups, handleFetchGroups } = useFetchAttachmentGroup();

const groupToUpdate = ref<Group | null>(null);
const loading = ref<boolean>(false);
const editingModal = ref(false);

const routeQuery = useRouteQuery("group");

const handleSelectGroup = (group: Group) => {
  emit("update:selectedGroup", group);
  emit("select", group);

  if (!props.readonly) {
    routeQuery.value = group.metadata.name;
  }
};

const handleOpenEditingModal = (group: Group) => {
  groupToUpdate.value = group;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  emit("update");
  handleFetchGroups();
};

onMounted(async () => {
  await handleFetchGroups();

  if (routeQuery.value && !props.readonly) {
    const allGroups = [...defaultGroups, ...groups.value];
    const group = allGroups.find(
      (group) => group.metadata.name === routeQuery.value
    );
    if (group) {
      handleSelectGroup(group);
    }
    return;
  }

  handleSelectGroup(defaultGroups[0]);
});
</script>
<template>
  <AttachmentGroupEditingModal
    v-model:visible="editingModal"
    :group="groupToUpdate"
    @close="onEditingModalClose"
  />
  <div class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-6">
    <div
      v-for="(defaultGroup, index) in defaultGroups"
      :key="index"
      :class="{
        '!bg-gray-200 !text-gray-900':
          defaultGroup.metadata.name === selectedGroup?.metadata.name,
      }"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="handleSelectGroup(defaultGroup)"
    >
      <div class="flex flex-1 items-center">
        <span class="text-sm">{{ defaultGroup.spec.displayName }}</span>
      </div>
    </div>
    <div
      v-for="(group, index) in groups"
      :key="index"
      :class="{
        '!bg-gray-200 !text-gray-900':
          group.metadata.name === selectedGroup?.metadata.name,
      }"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="handleSelectGroup(group)"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="truncate text-sm">
          {{ group.spec.displayName }}
        </span>
      </div>
      <FloatingDropdown v-if="!readonly">
        <IconMore />
        <template #popper>
          <div class="w-48 p-2">
            <VSpace class="w-full" direction="column">
              <VButton
                v-close-popper
                block
                type="secondary"
                @click="handleOpenEditingModal(group)"
              >
                重命名
              </VButton>
              <VButton v-close-popper block type="danger"> 删除</VButton>
            </VSpace>
          </div>
        </template>
      </FloatingDropdown>
    </div>
    <div
      v-if="!loading && !readonly"
      class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
      @click="editingModal = true"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="truncate text-sm">添加分组</span>
      </div>
      <IconAddCircle />
    </div>
  </div>
</template>
