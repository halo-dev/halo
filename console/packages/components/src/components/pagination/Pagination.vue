<script lang="ts" setup>
import { computed } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";

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

const emit = defineEmits<{
  (event: "update:page", page: number): void;
  (event: "update:size", size: number): void;
  (event: "change", value: { page: number; size: number }): void;
}>();

const totalPages = computed(() => Math.ceil(props.total / props.size));

const hasNext = computed(() => props.page < totalPages.value);

const hasPrevious = computed(() => props.page > 1);

const onPageChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  const page = Number(target.value);
  emit("update:page", page);
  emit("change", { page, size: props.size });
};

const onSizeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  const size = Number(target.value);
  emit("update:size", size);

  // reset page to 1
  emit("update:page", 1);
  emit("change", { page: 1, size });
};

const previous = () => {
  if (hasPrevious.value) {
    const page = props.page - 1;
    emit("update:page", page);
    emit("change", { page: page, size: props.size });
  }
};

const next = () => {
  if (hasNext.value) {
    const page = props.page + 1;
    emit("update:page", page);
    emit("change", { page: page, size: props.size });
  }
};
</script>
<template>
  <div class="bg-white flex items-center justify-between">
    <div class="flex-1 flex justify-between sm:!hidden items-center">
      <button
        class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 cursor-pointer"
        :disabled="!hasPrevious"
        @click="previous"
      >
        <IconArrowLeft />
      </button>
      <span class="text-sm text-gray-500"> {{ page }} / {{ totalPages }} </span>
      <button
        class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 cursor-pointer"
        :disabled="!hasNext"
        @click="next"
      >
        <IconArrowRight />
      </button>
    </div>
    <div class="hidden sm:flex-1 sm:flex sm:items-center items-center gap-2">
      <nav
        aria-label="Pagination"
        class="relative z-0 inline-flex rounded-base shadow-sm -space-x-px"
      >
        <button
          class="relative h-8 outline-none inline-flex items-center px-2 py-1.5 rounded-l-base border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer disabled:cursor-not-allowed"
          :disabled="!hasPrevious"
          @click="previous"
        >
          <IconArrowLeft />
        </button>
        <button
          class="relative h-8 outline-none inline-flex items-center px-2 py-1.5 rounded-r-base border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 cursor-pointer disabled:cursor-not-allowed"
          :disabled="!hasNext"
          @click="next"
        >
          <IconArrowRight />
        </button>
      </nav>
      <div class="inline-flex items-center gap-2">
        <select
          :value="page"
          :disabled="totalPages === 0"
          class="h-8 border outline-none rounded-base px-2 text-gray-800 text-sm border-gray-300"
          @change="onPageChange"
        >
          <option v-if="totalPages === 0" :value="0">0 / 0</option>
          <option v-for="i in totalPages" :key="i" :value="i">
            {{ i }} / {{ totalPages }}
          </option>
        </select>
        <span class="text-sm text-gray-500">{{ pageLabel }}</span>
      </div>
      <div class="inline-flex items-center gap-2">
        <select
          :value="size"
          class="h-8 border outline-none rounded-base px-2 text-gray-800 text-sm border-gray-300"
          @change="onSizeChange"
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
