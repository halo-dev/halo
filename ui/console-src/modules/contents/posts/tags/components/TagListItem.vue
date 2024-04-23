<script lang="ts" setup>
import HasPermission from "@/components/permission/HasPermission.vue";
import { formatDatetime } from "@/utils/date";
import type { Tag } from "@halo-dev/api-client";
import {
  VStatusDot,
  VEntity,
  VEntityField,
  VDropdownItem,
  IconExternalLinkLine,
  VSpace,
} from "@halo-dev/components";
import PostTag from "./PostTag.vue";

withDefaults(
  defineProps<{
    tag: Tag;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const emit = defineEmits<{
  (event: "editing", tag: Tag): void;
  (event: "delete", tag: Tag): void;
}>();
</script>
<template>
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <HasPermission :permissions="['system:posts:manage']">
        <slot name="checkbox" />
      </HasPermission>
    </template>

    <template #start>
      <VEntityField>
        <template #title>
          <PostTag :tag="tag" />
        </template>
        <template #description>
          <VSpace>
            <div
              v-if="tag.status?.permalink"
              :title="tag.status?.permalink"
              target="_blank"
              class="truncate text-xs text-gray-500 group-hover:text-gray-900"
            >
              {{ tag.status.permalink }}
            </div>
            <a
              target="_blank"
              :href="tag.status?.permalink"
              class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
            >
              <IconExternalLinkLine class="h-3.5 w-3.5" />
            </a>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="tag.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField
        :description="
          $t('core.common.fields.post_count', {
            count: tag.status?.postCount || 0,
          })
        "
      />
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(tag.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <HasPermission :permissions="['system:posts:manage']">
        <VDropdownItem @click="emit('editing', tag)">
          {{ $t("core.common.buttons.edit") }}
        </VDropdownItem>
        <VDropdownItem type="danger" @click="emit('delete', tag)">
          {{ $t("core.common.buttons.delete") }}
        </VDropdownItem>
      </HasPermission>
    </template>
  </VEntity>
</template>
