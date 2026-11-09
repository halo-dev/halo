<script lang="ts" setup>
import { useAllPostTagsQuery } from "@console/modules/contents/posts/tags/composables/use-post-tag";
import {
  VFilterDropdown,
  type FilterDropdownOption,
  type FilterDropdownValue,
} from "@halo-dev/components";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

withDefaults(
  defineProps<{
    label: string;
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const { t } = useI18n();
const { data: tags } = useAllPostTagsQuery();

const tagMap = computed(() => {
  return new Map(tags.value?.map((tag) => [tag.metadata.name, tag]));
});

const options = computed<FilterDropdownOption[]>(() => {
  return (
    tags.value?.map((tag) => ({
      label: tag.spec.displayName,
      value: tag.metadata.name,
      description: tag.status?.permalink,
      keywords: [tag.metadata.name],
    })) || []
  );
});

function getTag(option: FilterDropdownOption) {
  if (option.value === undefined) {
    return undefined;
  }

  return tagMap.value.get(String(option.value));
}

function getPostCount(option: FilterDropdownOption) {
  return t("core.common.fields.post_count", {
    count: getTag(option)?.status?.postCount || 0,
  });
}

function handleUpdate(value?: FilterDropdownValue) {
  emit("update:modelValue", typeof value === "string" ? value : undefined);
}
</script>

<template>
  <VFilterDropdown
    :model-value="modelValue"
    :label="label"
    :options="options"
    searchable
    :search-placeholder="$t('core.common.placeholder.search')"
    :empty-text="$t('core.components.global_search.no_results')"
    panel-width="22rem"
    @update:model-value="handleUpdate"
  >
    <template #option="{ option, selected }">
      <span
        class="h-2.5 w-2.5 shrink-0 rounded-full"
        :style="{
          backgroundColor: getTag(option)?.spec.color || '#d9d9d9',
        }"
      />
      <div class="flex min-w-0 flex-1 flex-col">
        <span
          class="truncate text-sm"
          :class="selected ? 'font-medium text-gray-900' : 'text-gray-700'"
        >
          {{ option.label }}
        </span>
        <span v-if="option.description" class="truncate text-xs text-gray-500">
          {{ option.description }}
        </span>
      </div>
      <span class="shrink-0 text-xs text-gray-500">
        {{ getPostCount(option) }}
      </span>
    </template>
  </VFilterDropdown>
</template>
