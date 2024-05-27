<script lang="ts" setup>
import type { Group } from "@halo-dev/api-client";
import { IconAddCircle } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import AttachmentGroupBadge from "./AttachmentGroupBadge.vue";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    readonly?: boolean;
  }>(),
  {
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "select", group: Group): void;
}>();

const queryClient = useQueryClient();

const defaultGroups: Group[] = [
  {
    spec: {
      displayName: t("core.attachment.group_list.internal_groups.all"),
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "",
    },
  },
  {
    spec: {
      displayName: t("core.attachment.common.text.ungrouped"),
    },
    apiVersion: "",
    kind: "",
    metadata: {
      name: "ungrouped",
    },
  },
];

const { groups } = useFetchAttachmentGroup();

const loading = ref<boolean>(false);
const creationModalVisible = ref(false);

const selectedGroup = props.readonly
  ? ref("")
  : useRouteQuery<string>("group", "");

const handleSelectGroup = (group: Group) => {
  emit("select", group);
  selectedGroup.value = group.metadata.name;
};

const onCreationModalClose = () => {
  queryClient.invalidateQueries({ queryKey: ["attachment-groups"] });
  creationModalVisible.value = false;
};
</script>
<template>
  <AttachmentGroupEditingModal
    v-if="!readonly && creationModalVisible"
    @close="onCreationModalClose"
  />
  <div
    class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 md:grid-cols-3 lg:grid-cols-4 2xl:grid-cols-6"
  >
    <AttachmentGroupBadge
      v-for="defaultGroup in defaultGroups"
      :key="defaultGroup.metadata.name"
      :group="defaultGroup"
      :is-selected="defaultGroup.metadata.name === selectedGroup"
      :features="{ actions: false, checkIcon: readonly }"
      @click="handleSelectGroup(defaultGroup)"
    />

    <AttachmentGroupBadge
      v-for="group in groups"
      :key="group.metadata.name"
      :group="group"
      :is-selected="group.metadata.name === selectedGroup"
      :features="{ actions: !readonly, checkIcon: readonly }"
      @click="handleSelectGroup(group)"
    />

    <HasPermission
      v-if="!loading && !readonly"
      :permissions="['system:attachments:manage']"
    >
      <AttachmentGroupBadge
        :features="{ actions: false }"
        @click="creationModalVisible = true"
      >
        <template #text>
          <span>{{ $t("core.common.buttons.new") }}</span>
        </template>
        <template #actions>
          <IconAddCircle />
        </template>
      </AttachmentGroupBadge>
    </HasPermission>
  </div>
</template>
