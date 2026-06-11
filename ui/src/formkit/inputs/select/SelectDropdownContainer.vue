<script lang="ts" setup>
import { VEmpty, VLoading } from "@halo-dev/components";
import { useTimeout } from "@vueuse/shared";
import { computed, watch } from "vue";
import { i18n } from "@/locales";
import { isSelectOptionMatched } from "./option-utils";
import SelectOption from "./SelectOption.vue";
import type { SelectOption as SelectOptionType } from "./types";

const props = defineProps<{
  options?: SelectOptionType[];
  keyword?: string;
  selectedOptions?: SelectOptionType[];
  multiple: boolean;
  loading: boolean;
  nextLoading: boolean;
  remote: boolean;
  allowCreate: boolean;
  maxCount: number;
}>();

const emit = defineEmits<{
  (event: "selected", value: SelectOptionType): void;
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

  const options = props.options.filter((option) =>
    isSelectOptionMatched(option, keyword)
  );

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

const { ready, start, stop } = useTimeout(200, { controls: true });

watch(
  () => props.loading,
  (loading) => {
    stop();
    if (loading) {
      start();
    }
  }
);

const handleLoadMore = () => {
  if (props.remote) {
    emit("loadMore");
  }
};
</script>

<template>
  <div class="w-full">
    <div v-if="ready && loading && !nextLoading">
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
      <VEmpty :title="i18n.global.t('core.formkit.select.no_data')"></VEmpty>
    </div>
  </div>
</template>
