<script lang="ts" setup>
import { UseOffsetPagination } from "@vueuse/components";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import { ref, toRefs, watch } from "vue";

const props = withDefaults(
  defineProps<{
    page?: number;
    size?: number;
    total?: number;
  }>(),
  {
    page: 1,
    size: 10,
    total: 0,
  }
);

const { page, size, total } = toRefs(props);

const key = ref(Math.random());

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
  emit("change", {
    page: currentPage,
    size: currentPageSize,
  });
};

watch(
  () => total?.value,
  () => {
    key.value = Math.random();
  }
);
</script>
<template>
  <UseOffsetPagination
    :key="key"
    v-slot="{ currentPage, next, prev, pageCount }"
    :page="page"
    :page-size="size"
    :total="total"
    @page-change="onPageChange"
    @page-size-change="onPageChange"
  >
    <div class="bg-white flex items-center justify-between">
      <div class="flex-1 flex justify-between sm:hidden items-center">
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
      <div class="hidden sm:flex-1 sm:flex sm:items-center">
        <nav
          aria-label="Pagination"
          class="relative z-0 inline-flex rounded-base shadow-sm -space-x-px"
        >
          <span
            class="relative inline-flex items-center px-2 py-2 rounded-l-[4px] border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer"
            @click="prev"
          >
            <IconArrowLeft />
          </span>
          <span
            v-for="i in pageCount"
            :key="i"
            :class="{
              'z-10 bg-primary/1 border-primary text-primary':
                i === currentPage,
              'bg-white border-gray-300 text-gray-500 hover:bg-gray-50':
                i !== currentPage,
            }"
            aria-current="page"
            class="relative inline-flex items-center px-4 py-2 border text-sm font-medium cursor-pointer select-none"
            @click="currentPage.value = i"
          >
            {{ i }}
          </span>
          <span
            class="relative inline-flex items-center px-2 py-2 rounded-r-[4px] border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer"
            @click="next"
          >
            <IconArrowRight />
          </span>
        </nav>
      </div>
    </div>
  </UseOffsetPagination>
</template>
