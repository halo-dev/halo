<script lang="ts" setup>
import { computed, onMounted, watch } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";

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
    prevLabel?: string;
    nextLabel?: string;
  }>(),
  {
    page: 1,
    size: 10,
    total: 0,
    sizeOptions: () => [10],
    showTotal: true,
    pageLabel: undefined,
    sizeLabel: undefined,
    totalLabel: undefined,
    prevLabel: undefined,
    nextLabel: undefined,
  }
);

const { t } = useComponentsI18n();

const emit = defineEmits<{
  (event: "update:page", page: number): void;
  (event: "update:size", size: number): void;
  (event: "change", value: { page: number; size: number }): void;
}>();

const DEFAULT_SIZE = 10;

const normalizeSize = (size: number) => {
  if (!Number.isFinite(size) || size <= 0) {
    return undefined;
  }

  const normalized = Math.floor(size);
  return normalized > 0 ? normalized : undefined;
};

const safeTotal = computed(() =>
  Number.isFinite(props.total) && props.total > 0 ? Math.floor(props.total) : 0
);

const fallbackSize = computed(() => {
  for (const option of props.sizeOptions) {
    const size = normalizeSize(option);
    if (size !== undefined) {
      return size;
    }
  }

  return DEFAULT_SIZE;
});

const currentSize = computed(
  () => normalizeSize(props.size) ?? fallbackSize.value
);

const totalPages = computed(() =>
  safeTotal.value === 0 ? 0 : Math.ceil(safeTotal.value / currentSize.value)
);

const currentPage = computed(() => {
  if (totalPages.value === 0) {
    return 1;
  }

  const page = Number.isFinite(props.page) ? Math.floor(props.page) : 1;
  return Math.min(Math.max(1, page), totalPages.value);
});

const hasNext = computed(() => currentPage.value < totalPages.value);

const hasPrevious = computed(() => currentPage.value > 1);

const sizeOptionsValue = computed(() => {
  const options: number[] = [];

  for (const option of [...props.sizeOptions, currentSize.value]) {
    const size = normalizeSize(option);
    if (size !== undefined && !options.includes(size)) {
      options.push(size);
    }
  }

  return options;
});

const pageLabelText = computed(() => props.pageLabel ?? t("pagination.page"));

const sizeLabelText = computed(() => props.sizeLabel ?? t("pagination.size"));

const prevLabelText = computed(() => props.prevLabel ?? t("common.previous"));

const nextLabelText = computed(() => props.nextLabel ?? t("common.next"));

const totalLabelText = computed(
  () => props.totalLabel ?? t("pagination.total", { total: safeTotal.value })
);

const goToPage = (page: number) => {
  if (totalPages.value === 0 || !Number.isFinite(page)) {
    return;
  }

  const target = Math.min(Math.max(1, Math.floor(page)), totalPages.value);

  if (target === currentPage.value) {
    return;
  }

  emit("update:page", target);
  emit("change", { page: target, size: currentSize.value });
};

const onPageChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  goToPage(Number(target.value));
};

const onSizeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  const size = normalizeSize(Number(target.value));

  if (size === undefined || size === currentSize.value) {
    return;
  }

  emit("update:size", size);

  // reset page to 1
  emit("update:page", 1);
  emit("change", { page: 1, size });
};

const previous = () => {
  if (hasPrevious.value) {
    goToPage(currentPage.value - 1);
  }
};

const next = () => {
  if (hasNext.value) {
    goToPage(currentPage.value + 1);
  }
};

const clampPage = () => {
  if (totalPages.value === 0) {
    return;
  }

  if (props.page !== currentPage.value) {
    emit("update:page", currentPage.value);
  }
};

const clampSize = () => {
  if (props.size !== currentSize.value) {
    emit("update:size", currentSize.value);
  }
};

onMounted(() => {
  clampSize();
  clampPage();
});

watch(currentSize, clampSize);
watch([() => props.page, totalPages], clampPage);
</script>
<template>
  <div class="pagination">
    <div v-if="showTotal" class="pagination__total">
      {{ totalLabelText }}
    </div>
    <div class="pagination__controller">
      <nav :aria-label="t('pagination.navigation')" class="pagination__nav">
        <button
          :aria-label="prevLabelText"
          :disabled="!hasPrevious"
          class="pagination__btn pagination__btn--prev"
          @click="previous"
        >
          <IconArrowLeft aria-hidden="true" />
        </button>
        <button
          :aria-label="nextLabelText"
          :disabled="!hasNext"
          class="pagination__btn pagination__btn--next"
          @click="next"
        >
          <IconArrowRight aria-hidden="true" />
        </button>
      </nav>
      <div class="pagination__select-wrap">
        <select
          :aria-label="pageLabelText"
          :value="currentPage"
          :disabled="totalPages === 0"
          class="pagination__select"
          @change="onPageChange"
        >
          <option v-if="totalPages === 0" :value="1">0 / 0</option>
          <option v-for="i in totalPages" :key="i" :value="i">
            {{ i }} / {{ totalPages }}
          </option>
        </select>
        <span class="pagination__select-label">
          {{ pageLabelText }}
        </span>
      </div>
      <div class="pagination__select-wrap">
        <select
          :aria-label="sizeLabelText"
          :value="currentSize"
          class="pagination__select"
          @change="onSizeChange"
        >
          <option
            v-for="sizeOption in sizeOptionsValue"
            :key="sizeOption"
            :value="sizeOption"
          >
            {{ sizeOption }}
          </option>
        </select>
        <span class="pagination__select-label">
          {{ sizeLabelText }}
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
    @apply relative inline-flex h-8 cursor-pointer items-center rounded-base border border-gray-300 bg-white px-2 py-1.5 text-sm font-medium text-gray-500 outline-none focus-visible:z-10 focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary hover:enabled:bg-gray-50 disabled:cursor-not-allowed disabled:bg-gray-50 disabled:text-gray-400 disabled:opacity-50;

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
    @apply h-8 cursor-pointer rounded-base border border-solid border-gray-300 bg-white px-2 pr-10 text-sm text-gray-800 outline-none focus:border-primary focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary disabled:cursor-not-allowed disabled:bg-gray-50 disabled:text-gray-400 disabled:opacity-50;
  }

  &__select-label {
    @apply text-sm text-gray-500;
  }
}
</style>
