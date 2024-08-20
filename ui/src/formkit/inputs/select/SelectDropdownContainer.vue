<script lang="ts" setup>
import { computed } from "vue";
import SelectOption from "./SelectOption.vue";
import { VEmpty, VLoading } from "@halo-dev/components";

const props = defineProps<{
  options?: Array<Record<string, unknown> & { label: string; value: string }>;
  keyword?: string;
  selectedOptions?: Array<{
    label: string;
    value: string;
  }>;
  multiple: boolean;
  loading: boolean;
  nextLoading: boolean;
  remote: boolean;
  allowCreate: boolean;
  maxCount: number;
}>();

const emit = defineEmits<{
  (
    event: "selected",
    value: Record<string, unknown> & { label: string; value: string }
  ): void;
  (event: "loadMore"): void;
}>();

const filterOptions = computed(() => {
  if (!props.options) {
    return [];
  }

  if (props.remote) {
    return props.options;
  }

  const keyword = props.keyword;
  if (!keyword) {
    return props.options;
  }

  const options = props.options.filter((option) => {
    return option.label
      .toLocaleLowerCase()
      .includes(keyword.toLocaleLowerCase());
  });

  if (props.allowCreate) {
    const hasKeyword = options.some((option) => {
      return option.value === keyword;
    });

    if (!hasKeyword) {
      options.unshift({
        label: keyword,
        value: keyword,
      });
    }
  }
  return options;
});

const handleLoadMore = () => {
  if (props.remote) {
    emit("loadMore");
  }
};
</script>

<template>
  <div class="w-full">
    <div v-if="loading && !nextLoading">
      <VLoading></VLoading>
    </div>
    <div v-else-if="filterOptions && filterOptions.length > 0">
      <SelectOption
        v-bind="$props"
        :options="filterOptions"
        @selected="(option) => emit('selected', option)"
        @load-more="handleLoadMore"
      ></SelectOption>
    </div>
    <div v-else>
      <VEmpty title="No Data"></VEmpty>
    </div>
  </div>
</template>
