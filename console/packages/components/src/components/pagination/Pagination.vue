<script lang="ts" setup>
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import { ref, watch } from "vue";
import { useOffsetPagination } from "@vueuse/core";

const props = withDefaults(
  defineProps<{
    page?: number;
    size?: number;
    total?: number;
    sizeOptions?: number[];
    pageLabel?: string;
    sizeLabel?: string;
  }>(),
  {
    page: 1,
    size: 10,
    total: 0,
    sizeOptions: () => [10],
    pageLabel: "页",
    sizeLabel: "条 / 页",
  }
);

const page = ref(props.page);
const size = ref(props.size);
const total = ref(props.total);

watch([() => props.page, () => props.size, () => props.total], () => {
  page.value = props.page;
  size.value = props.size;
  total.value = props.total;
});

const emit = defineEmits<{
  (event: "update:page", page: number): void;
  (event: "update:size", size: number): void;
  (event: "change", value: { page: number; size: number }): void;
}>();

const onPageChange = ({
  currentPage,
  currentPageSize,
}: {
  currentPage: number;
  currentPageSize: number;
}) => {
  emit("update:page", currentPage);
  emit("update:size", currentPageSize);
  emit("change", {
    page: currentPage,
    size: currentPageSize,
  });
};

const {
  currentPage,
  currentPageSize,
  pageCount,
  isFirstPage,
  isLastPage,
  prev,
  next,
} = useOffsetPagination({
  total: total,
  page: page,
  pageSize: size,
  onPageChange: onPageChange,
  onPageSizeChange: onPageChange,
});
</script>
<template>
  <div class="bg-white flex items-center justify-between">
    <div class="flex-1 flex justify-between sm:!hidden items-center">
      <span
        class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 cursor-pointer"
        @click="prev"
      >
        <IconArrowLeft />
      </span>
      <span class="text-sm text-gray-500">
        {{ currentPage }} / {{ pageCount }}
      </span>
      <span
        class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 cursor-pointer"
        @click="next"
      >
        <IconArrowRight />
      </span>
    </div>
    <div class="hidden sm:flex-1 sm:flex sm:items-center items-center gap-2">
      <nav
        aria-label="Pagination"
        class="relative z-0 inline-flex rounded-base shadow-sm -space-x-px"
      >
        <button
          class="relative h-8 outline-none inline-flex items-center px-2 py-1.5 rounded-l-base border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer disabled:cursor-not-allowed"
          :disabled="isFirstPage"
          @click="prev"
        >
          <IconArrowLeft />
        </button>
        <button
          class="relative h-8 outline-none inline-flex items-center px-2 py-1.5 rounded-r-base border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer disabled:cursor-not-allowed"
          :disabled="isLastPage"
          @click="next"
        >
          <IconArrowRight />
        </button>
      </nav>
      <div class="inline-flex items-center gap-2">
        <select
          v-model="currentPage"
          :disabled="pageCount === 0"
          class="h-8 border outline-none rounded-base px-2 text-gray-800 text-sm border-gray-300"
        >
          <option v-if="pageCount === 0" :value="0">0 / 0</option>
          <option v-for="i in pageCount" :key="i" :value="i">
            {{ i }} / {{ pageCount }}
          </option>
        </select>
        <span class="text-sm text-gray-500">{{ pageLabel }}</span>
      </div>
      <div class="inline-flex items-center gap-2">
        <select
          v-model="currentPageSize"
          class="h-8 border outline-none rounded-base px-2 text-gray-800 text-sm border-gray-300"
        >
          <option
            v-for="(sizeOption, index) in sizeOptions"
            :key="index"
            :value="sizeOption"
          >
            {{ sizeOption }}
          </option>
        </select>
        <span class="text-sm text-gray-500">{{ sizeLabel }}</span>
      </div>
    </div>
  </div>
</template>
