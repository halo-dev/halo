<script lang="ts" setup>
import { computed } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";

const props = withDefaults(
  defineProps<{
    page?: number;
    size?: number;
    total?: number;
    sizeOptions?: number[];
    showTotal?: boolean;
    pageLabel?: string;
    sizeLabel?: string;
    totalLabel?: string;
  }>(),
  {
    page: 1,
    size: 10,
    total: 0,
    sizeOptions: () => [10],
    showTotal: true,
    pageLabel: "页",
    sizeLabel: "条 / 页",
    totalLabel: undefined,
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

const totalLabelText = computed(() => {
  if (props.totalLabel) {
    return props.totalLabel;
  }
  return `共 ${props.total} 项数据`;
});

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
  <div class="pagination">
    <div v-if="showTotal" class="pagination__total">
      {{ totalLabelText }}
    </div>
    <div class="pagination__controller">
      <nav aria-label="Pagination" class="pagination__nav">
        <button
          class="pagination__btn pagination__btn--prev"
          :disabled="!hasPrevious"
          @click="previous"
        >
          <IconArrowLeft />
        </button>
        <button
          class="pagination__btn pagination__btn--next"
          :disabled="!hasNext"
          @click="next"
        >
          <IconArrowRight />
        </button>
      </nav>
      <div class="pagination__select-wrap">
        <select
          :value="page"
          :disabled="totalPages === 0"
          class="pagination__select"
          @change="onPageChange"
        >
          <option v-if="totalPages === 0" :value="1">0 / 0</option>
          <option v-for="i in totalPages || 1" :key="i" :value="i">
            {{ i }} / {{ totalPages }}
          </option>
        </select>
        <span class="pagination__select-label">
          {{ pageLabel }}
        </span>
      </div>
      <div class="pagination__select-wrap">
        <select :value="size" class="pagination__select" @change="onSizeChange">
          <option
            v-for="(sizeOption, index) in sizeOptions"
            :key="index"
            :value="sizeOption"
          >
            {{ sizeOption }}
          </option>
        </select>
        <span class="pagination__select-label">
          {{ sizeLabel }}
        </span>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.pagination {
  @apply flex flex-1 items-center gap-2 bg-white;

  &__total {
    @apply hidden text-sm text-gray-500 sm:block;
  }

  &__controller {
    @apply flex flex-1 items-center justify-end gap-2;
  }

  &__nav {
    @apply relative z-0 inline-flex -space-x-px rounded-base shadow-sm;
  }

  &__btn {
    @apply relative inline-flex h-8 cursor-pointer items-center rounded-base border border-gray-300 bg-white px-2 py-1.5 text-sm font-medium text-gray-500 outline-none hover:bg-gray-50 disabled:cursor-not-allowed;

    &--prev {
      @apply rounded-r-none;
    }

    &--next {
      @apply rounded-l-none;
    }
  }

  &__select-wrap {
    @apply inline-flex items-center gap-2;
  }

  &__select {
    @apply h-8 rounded-base border border-solid border-gray-300 px-2 pr-10 text-sm text-gray-800 outline-none focus:border-primary;
  }

  &__select-label {
    @apply text-sm text-gray-500;
  }
}
</style>
