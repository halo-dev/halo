<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { drop, take } from "es-toolkit";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useFetchAttachmentGroup } from "../../../composables/use-attachment-group";
import AttachmentGroupBadge from "../../AttachmentGroupBadge.vue";
import AttachmentGroupEditingModal from "../../AttachmentGroupEditingModal.vue";

const modelValue = defineModel<string | undefined>();

const { t } = useI18n();
const queryClient = useQueryClient();

const { groups } = useFetchAttachmentGroup();

const creationModalVisible = ref(false);

const onCreationModalClose = () => {
  queryClient.invalidateQueries({ queryKey: ["attachment-groups"] });
  creationModalVisible.value = false;
};

const allGroups = computed(() => {
  return [
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
    ...(groups.value || []),
  ];
});

const latestGroups = computed(() => {
  return take(allGroups.value, 7);
});

const moreGroups = computed(() => {
  return drop(allGroups.value, 7);
});
</script>
<template>
  <div class="flex flex-col gap-2">
    <div class="text-sm text-gray-800">
      {{ $t("core.attachment.upload_modal.filters.group.label") }}
    </div>
    <div class="grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-5">
      <AttachmentGroupBadge
        :is-selected="!modelValue"
        :features="{ actions: false, checkIcon: true }"
        @click="modelValue = undefined"
      >
        <template #text>
          {{ $t("core.attachment.group_list.internal_groups.all") }}
        </template>
      </AttachmentGroupBadge>

      <AttachmentGroupBadge
        v-for="group in latestGroups"
        :key="group.metadata.name"
        :group="group"
        :is-selected="group.metadata.name === modelValue"
        :features="{ actions: false, checkIcon: true }"
        @click="modelValue = group.metadata.name"
      />

      <VDropdown v-if="moreGroups.length > 0">
        <AttachmentGroupBadge :features="{ actions: false, checkIcon: false }">
          <template #text> {{ $t("core.common.buttons.more") }} </template>
          <template #actions>
            <IconArrowDown />
          </template>
        </AttachmentGroupBadge>
        <template #popper>
          <VDropdownItem
            v-for="value in moreGroups"
            :key="value.metadata.name"
            :selected="value.metadata.name === modelValue"
            @click="modelValue = value.metadata.name"
          >
            {{ value.spec.displayName }}
          </VDropdownItem>
        </template>
      </VDropdown>

      <HasPermission :permissions="['system:attachments:manage']">
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
  </div>

  <AttachmentGroupEditingModal
    v-if="creationModalVisible"
    @close="onCreationModalClose"
  />
</template>
