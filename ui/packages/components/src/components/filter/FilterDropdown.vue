<script lang="ts" setup>
import {
  computed,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  useId,
  watch,
} from "vue";
import {
  IconArrowDown,
  IconArrowRight,
  IconCheck,
  IconSearch,
} from "../../icons/icons";
import { VDropdown, type VDropdownPlacement } from "../dropdown";
import { VLoading } from "../loading";
import { filterDropdownGroupKey } from "./group";
import type {
  FilterDropdownGroup,
  FilterDropdownItem,
  FilterDropdownOption,
  FilterDropdownValue,
  FilterDropdownVariant,
} from "./types";

const props = withDefaults(
  defineProps<{
    /**
     * Text displayed on the trigger, for example "Author".
     */
    label: string;
    /**
     * The selected value.
     */
    modelValue?: FilterDropdownValue;
    /**
     * Options rendered in the panel. Items can be options or groups.
     */
    options?: FilterDropdownItem[];
    /**
     * Optional title displayed at the top of the panel.
     */
    title?: string;
    /**
     * Whether to display a search input.
     */
    searchable?: boolean;
    /**
     * When enabled, options are not filtered locally. The `search` event is
     * emitted with the debounced keyword so the options can be loaded remotely.
     */
    remoteSearch?: boolean;
    /**
     * Placeholder of the search input.
     */
    searchPlaceholder?: string;
    /**
     * Text displayed when there are no options.
     */
    emptyText?: string;
    /**
     * Delay in milliseconds before emitting the `search` event.
     */
    searchDelay?: number;
    /**
     * Whether options are being loaded. Used together with `hasMore`.
     */
    loading?: boolean;
    /**
     * Whether more options can be loaded. When true, the `load-more` event is
     * emitted as the user scrolls near the bottom of the panel.
     */
    hasMore?: boolean;
    /**
     * Whether the trigger is disabled.
     */
    disabled?: boolean;
    /**
     * Whether to display a clear option at the top of the panel. Selecting it
     * clears the current value.
     */
    showClearOption?: boolean;
    /**
     * Label of the clear option.
     */
    clearLabel?: string;
    /**
     * Label of the selected value. Useful when the selected option is not in
     * the current options, for example when options are loaded remotely.
     */
    selectedLabel?: string;
    /**
     * Placement of the panel.
     */
    placement?: VDropdownPlacement;
    /**
     * Width of the panel, for example "20rem". By default the panel width is
     * sized by its content, between 12rem and 20rem.
     */
    panelWidth?: string;
    /**
     * Style of the trigger button.
     */
    variant?: FilterDropdownVariant;
  }>(),
  {
    modelValue: undefined,
    options: () => [],
    title: undefined,
    searchable: false,
    remoteSearch: false,
    searchPlaceholder: "Search...",
    emptyText: "No results",
    searchDelay: 300,
    loading: false,
    hasMore: false,
    disabled: false,
    showClearOption: false,
    clearLabel: "All",
    selectedLabel: undefined,
    placement: "bottom-start",
    panelWidth: undefined,
    variant: "default",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: FilterDropdownValue | undefined): void;
  (event: "select", option: FilterDropdownOption | undefined): void;
  (event: "search", keyword: string): void;
  (event: "load-more"): void;
}>();

defineSlots<{
  trigger?: (props: {
    selectedOption?: FilterDropdownOption;
    label: string;
    isSelected: boolean;
  }) => unknown;
  option?: (props: {
    option: FilterDropdownOption;
    selected: boolean;
    active: boolean;
    depth: number;
    expanded: boolean;
    hasChildren: boolean;
  }) => unknown;
  empty?: () => unknown;
  loading?: () => unknown;
}>();

interface OptionRow {
  kind: "option";
  key: string;
  id: string;
  option: FilterDropdownOption;
  depth: number;
  hasChildren: boolean;
  expanded: boolean;
  isClear?: boolean;
}

interface GroupRow {
  kind: "group";
  key: string;
  label?: string;
}

type Row = OptionRow | GroupRow;

const uid = useId();
const listboxId = `${uid}-listbox`;

const group = inject(filterDropdownGroupKey);
const groupEntryId = Symbol("filter-dropdown");

const dropdown = ref<{ show: () => void; hide: () => void }>();
const triggerRef = ref<HTMLButtonElement>();
const searchInputRef = ref<HTMLInputElement>();
const listRef = ref<HTMLElement>();

const isOpen = ref(false);
const keyword = ref("");
const activeKey = ref<string>();
const collapsedKeys = ref(new Set<string>());

const triggerClasses = computed(() => {
  const isSelected = props.modelValue !== undefined;
  const highlighted = isSelected || isOpen.value;

  return [
    "inline-flex h-8 cursor-pointer select-none items-center whitespace-nowrap rounded-md text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-60",
    highlighted ? "text-gray-900" : "text-gray-700",
    isSelected ? "font-semibold" : "",
    props.variant === "outline"
      ? [
          "border bg-white px-3",
          isOpen.value
            ? "border-primary"
            : "border-gray-300 hover:border-gray-400 hover:text-gray-900",
        ]
      : [
          "px-2 active:bg-gray-200",
          isOpen.value
            ? "bg-gray-100"
            : "hover:bg-gray-100 hover:text-gray-900",
        ],
  ];
});

const showSearch = computed(() => props.searchable || props.remoteSearch);
const normalizedKeyword = computed(() => keyword.value.trim().toLowerCase());

function isGroup(item: FilterDropdownItem): item is FilterDropdownGroup {
  return Array.isArray((item as FilterDropdownGroup).options);
}

function containsTree(items: FilterDropdownItem[]): boolean {
  return items.some((item) =>
    isGroup(item) ? containsTree(item.options) : !!item.children?.length
  );
}

const hasTree = computed(() => containsTree(props.options));

function matchesKeyword(text: string | undefined, word: string) {
  return !!text && text.toLowerCase().includes(word);
}

function matchesOption(option: FilterDropdownOption, word: string) {
  return (
    matchesKeyword(option.label, word) ||
    matchesKeyword(option.description, word) ||
    option.keywords?.some((keyword) => matchesKeyword(keyword, word))
  );
}

function filterOption(
  option: FilterDropdownOption,
  word: string
): FilterDropdownOption | undefined {
  const matchesSelf = matchesOption(option, word);

  if (!option.children?.length) {
    return matchesSelf ? option : undefined;
  }

  if (matchesSelf) {
    return option;
  }

  const children = option.children
    .map((child) => filterOption(child, word))
    .filter((child): child is FilterDropdownOption => !!child);

  if (!children.length) {
    return undefined;
  }

  return {
    ...option,
    children,
  };
}

function filterGroup(
  group: FilterDropdownGroup,
  word: string
): FilterDropdownGroup | undefined {
  if (matchesKeyword(group.label, word)) {
    return group;
  }

  const options = group.options
    .map((option) => filterOption(option, word))
    .filter((option): option is FilterDropdownOption => !!option);

  if (!options.length) {
    return undefined;
  }

  return {
    ...group,
    options,
  };
}

const filteredItems = computed<FilterDropdownItem[]>(() => {
  const word = normalizedKeyword.value;

  if (!word || props.remoteSearch) {
    return props.options;
  }

  return props.options
    .map((item) =>
      isGroup(item) ? filterGroup(item, word) : filterOption(item, word)
    )
    .filter((item): item is FilterDropdownItem => !!item);
});

function isExpanded(key: string) {
  return !!normalizedKeyword.value || !collapsedKeys.value.has(key);
}

const visibleRows = computed<Row[]>(() => {
  const rows: Row[] = [];

  if (props.showClearOption && !normalizedKeyword.value) {
    rows.push({
      kind: "option",
      key: "clear",
      id: `${listboxId}-option-clear`,
      option: { label: props.clearLabel },
      depth: 0,
      hasChildren: false,
      expanded: false,
      isClear: true,
    });
  }

  function pushOption(
    option: FilterDropdownOption,
    depth: number,
    key: string
  ) {
    const hasChildren = !!option.children?.length;
    const expanded = hasChildren && isExpanded(key);

    rows.push({
      kind: "option",
      key,
      id: `${listboxId}-option-${rows.length}`,
      option,
      depth,
      hasChildren,
      expanded,
    });

    if (!expanded) {
      return;
    }

    option.children?.forEach((child, index) => {
      pushOption(child, depth + 1, `${key}-${index}`);
    });
  }

  filteredItems.value.forEach((item, index) => {
    if (isGroup(item)) {
      if (item.label) {
        rows.push({
          kind: "group",
          key: `group-${index}`,
          label: item.label,
        });
      }

      item.options.forEach((option, optionIndex) => {
        pushOption(option, 0, `group-${index}-${optionIndex}`);
      });
      return;
    }

    pushOption(item, 0, `${index}`);
  });

  return rows;
});

const optionRows = computed(() =>
  visibleRows.value.filter((row): row is OptionRow => row.kind === "option")
);

const dataOptionRows = computed(() =>
  optionRows.value.filter((row) => !row.isClear)
);

const navigableRows = computed(() =>
  optionRows.value.filter((row) => !row.option.disabled)
);

const activeRow = computed(() =>
  navigableRows.value.find((row) => row.key === activeKey.value)
);

const activeOptionId = computed(() => activeRow.value?.id);

function isSelectable(option: FilterDropdownOption) {
  return option.value !== undefined;
}

function isSelected(option: FilterDropdownOption) {
  return isSelectable(option) && option.value === props.modelValue;
}

function isRowSelected(row: OptionRow) {
  if (row.isClear) {
    return props.modelValue === undefined;
  }

  return isSelected(row.option);
}

function findOptionByValue(
  items: FilterDropdownItem[],
  value: FilterDropdownValue
): FilterDropdownOption | undefined {
  for (const item of items) {
    if (isGroup(item)) {
      const found = findOptionByValue(item.options, value);
      if (found) {
        return found;
      }
      continue;
    }

    if (item.value === value) {
      return item;
    }

    if (item.children?.length) {
      const found = findOptionByValue(item.children, value);
      if (found) {
        return found;
      }
    }
  }

  return undefined;
}

const selectedOption = computed<FilterDropdownOption | undefined>(() => {
  if (props.modelValue === undefined) {
    return props.showClearOption ? { label: props.clearLabel } : undefined;
  }

  const option = findOptionByValue(props.options, props.modelValue);

  if (option) {
    return option;
  }

  return {
    label: props.selectedLabel ?? String(props.modelValue),
    value: props.modelValue,
  };
});

function show() {
  dropdown.value?.show();
}

function hide() {
  dropdown.value?.hide();
}

function toggleRow(row: OptionRow) {
  // Search always expands matching rows, so persisting a toggle here would
  // write a filtered row key that maps to an unrelated option afterwards.
  if (!row.hasChildren || normalizedKeyword.value) {
    return;
  }

  const next = new Set(collapsedKeys.value);

  if (next.has(row.key)) {
    next.delete(row.key);
  } else {
    next.add(row.key);
  }

  collapsedKeys.value = next;
  activeKey.value = row.key;
}

function clearSelection() {
  if (props.modelValue === undefined) {
    return;
  }

  emit("update:modelValue", undefined);
  emit("select", undefined);
}

function selectRow(row: OptionRow) {
  if (row.option.disabled) {
    return;
  }

  if (row.isClear) {
    clearSelection();
    keyword.value = "";
    hideAndRestoreFocus();
    return;
  }

  if (!isSelectable(row.option)) {
    toggleRow(row);
    return;
  }

  if (row.option.value === props.modelValue) {
    clearSelection();
  } else {
    emit("update:modelValue", row.option.value);
    emit("select", row.option);
  }

  keyword.value = "";
  hideAndRestoreFocus();
}

function moveActive(delta: number) {
  const rows = navigableRows.value;

  if (!rows.length) {
    return;
  }

  const currentIndex = rows.findIndex((row) => row.key === activeKey.value);

  let nextIndex: number;

  if (currentIndex === -1) {
    nextIndex = delta > 0 ? 0 : rows.length - 1;
  } else {
    nextIndex = Math.min(Math.max(currentIndex + delta, 0), rows.length - 1);
  }

  activeKey.value = rows[nextIndex]?.key;

  nextTick(scrollActiveIntoView);
}

function scrollActiveIntoView() {
  const row = activeRow.value;

  if (!row) {
    return;
  }

  listRef.value
    ?.querySelector<HTMLElement>(`[data-option-id="${row.id}"]`)
    ?.scrollIntoView?.({ block: "nearest" });
}

function onSearchKeydown(event: KeyboardEvent) {
  if (event.isComposing) {
    return;
  }

  const row = activeRow.value;

  switch (event.key) {
    case "ArrowDown":
      event.preventDefault();
      moveActive(1);
      break;
    case "ArrowUp":
      event.preventDefault();
      moveActive(-1);
      break;
    case "ArrowRight":
      if (!normalizedKeyword.value && row?.hasChildren && !row.expanded) {
        event.preventDefault();
        toggleRow(row);
      }
      break;
    case "ArrowLeft":
      if (!normalizedKeyword.value && row?.hasChildren && row.expanded) {
        event.preventDefault();
        toggleRow(row);
      }
      break;
    case "Enter":
      if (row) {
        event.preventDefault();
        selectRow(row);
      }
      break;
    case "Escape":
      event.preventDefault();
      suppressEscapeKeyup = true;
      hideAndRestoreFocus();
      break;
    case "Tab":
      hideAndRestoreFocus();
      break;
  }
}

function onListKeydown(event: KeyboardEvent) {
  const row = activeRow.value;

  if (event.isComposing) {
    return;
  }

  switch (event.key) {
    case "ArrowDown":
      event.preventDefault();
      moveActive(1);
      break;
    case "ArrowUp":
      event.preventDefault();
      moveActive(-1);
      break;
    case "Home":
      event.preventDefault();
      activeKey.value = navigableRows.value[0]?.key;
      nextTick(scrollActiveIntoView);
      break;
    case "End":
      event.preventDefault();
      activeKey.value = navigableRows.value.at(-1)?.key;
      nextTick(scrollActiveIntoView);
      break;
    case "ArrowRight":
      if (row?.hasChildren && !row.expanded) {
        event.preventDefault();
        toggleRow(row);
      }
      break;
    case "ArrowLeft":
      if (row?.hasChildren && row.expanded) {
        event.preventDefault();
        toggleRow(row);
      }
      break;
    case "Enter":
      if (row) {
        event.preventDefault();
        selectRow(row);
      }
      break;
    case " ":
      if (row) {
        event.preventDefault();
        selectRow(row);
      }
      break;
    case "Escape":
      event.preventDefault();
      suppressEscapeKeyup = true;
      hideAndRestoreFocus();
      break;
    case "Tab":
      hideAndRestoreFocus();
      break;
  }
}

let lastAutoLoadSignature = "";
let loadMoreRequested = false;
let searchTimer: ReturnType<typeof setTimeout> | undefined;

watch(keyword, (value) => {
  if (!props.remoteSearch) {
    return;
  }

  lastAutoLoadSignature = "";

  if (searchTimer) {
    clearTimeout(searchTimer);
  }

  searchTimer = setTimeout(() => {
    emit("search", value.trim());
  }, props.searchDelay);
});

watch(navigableRows, (rows) => {
  if (rows.some((row) => row.key === activeKey.value)) {
    return;
  }

  activeKey.value = normalizedKeyword.value ? rows[0]?.key : undefined;
});

function emitLoadMore() {
  if (!props.hasMore || props.loading || loadMoreRequested) {
    return;
  }

  loadMoreRequested = true;
  emit("load-more");
}

function countOptions(items: FilterDropdownItem[]): number {
  return items.reduce((total, item) => {
    if (isGroup(item)) {
      return total + countOptions(item.options);
    }

    return total + 1 + (item.children ? countOptions(item.children) : 0);
  }, 0);
}

const optionCount = computed(() => countOptions(props.options));

function maybeAutoLoadMore() {
  if (!props.hasMore || props.loading) {
    return;
  }

  const el = listRef.value;

  if (!el || el.scrollHeight > el.clientHeight + 8) {
    return;
  }

  const signature = `${keyword.value}|${optionCount.value}|${visibleRows.value.length}`;

  if (lastAutoLoadSignature === signature) {
    return;
  }

  lastAutoLoadSignature = signature;
  emitLoadMore();
}

function onScroll(event: Event) {
  const target = event.target as HTMLElement;

  if (target.scrollHeight - target.scrollTop - target.clientHeight <= 48) {
    emitLoadMore();
  } else {
    loadMoreRequested = false;
  }
}

async function onShow() {
  isOpen.value = true;
  lastAutoLoadSignature = "";
  loadMoreRequested = false;
}

async function onApplyShow() {
  await nextTick();

  if (showSearch.value) {
    searchInputRef.value?.focus();
  } else {
    listRef.value?.focus();
  }

  const selectedRow = optionRows.value.find((row) => isRowSelected(row));
  activeKey.value = selectedRow?.key;

  await nextTick();
  scrollActiveIntoView();
  maybeAutoLoadMore();
}

let suppressEscapeKeyup = false;

function hideAndRestoreFocus() {
  hide();
  nextTick(() => triggerRef.value?.focus());
}

function onTriggerKeydown(event: KeyboardEvent) {
  if (event.key === "Escape") {
    suppressEscapeKeyup = false;
  }
}

function onTriggerKeyup(event: KeyboardEvent) {
  if (!suppressEscapeKeyup) {
    return;
  }

  suppressEscapeKeyup = false;

  if (event.key === "Escape") {
    event.stopPropagation();
  }
}

function onHide() {
  isOpen.value = false;
  activeKey.value = undefined;
  lastAutoLoadSignature = "";
  loadMoreRequested = false;
}

watch(
  () => [props.options, optionCount.value, props.loading] as const,
  () => {
    loadMoreRequested = false;
  }
);

watch(
  () =>
    [
      props.options,
      optionCount.value,
      visibleRows.value.length,
      props.loading,
      isOpen.value,
    ] as const,
  async () => {
    if (!isOpen.value) {
      return;
    }

    await nextTick();
    maybeAutoLoadMore();
  }
);

onMounted(() => {
  group?.register(groupEntryId, {
    isActive: () => props.modelValue !== undefined,
    clear: clearSelection,
  });
});

onBeforeUnmount(() => {
  group?.unregister(groupEntryId);

  if (searchTimer) {
    clearTimeout(searchTimer);
  }
});

defineExpose({
  show,
  hide,
});
</script>

<template>
  <VDropdown
    ref="dropdown"
    :disabled="disabled"
    :placement="placement"
    :distance="4"
    :no-auto-focus="true"
    popper-class="[&_.v-popper\_\_inner]:!overflow-hidden [&_.v-popper\_\_inner]:!p-0"
    @show="onShow"
    @apply-show="onApplyShow"
    @hide="onHide"
  >
    <button
      ref="triggerRef"
      type="button"
      :class="triggerClasses"
      :disabled="disabled"
      :aria-haspopup="hasTree ? 'tree' : 'listbox'"
      :aria-expanded="isOpen"
      :aria-controls="listboxId"
      @keydown.down.prevent="show"
      @keydown="onTriggerKeydown"
      @keyup="onTriggerKeyup"
    >
      <slot
        name="trigger"
        :selected-option="selectedOption"
        :label="label"
        :is-selected="modelValue !== undefined"
      >
        <span v-if="!selectedOption" class="mr-0.5">
          {{ label }}
        </span>
        <span v-else class="mr-0.5">
          {{ label }}：{{ selectedOption.label }}
        </span>
        <span>
          <IconArrowDown />
        </span>
      </slot>
    </button>
    <template #popper>
      <div
        class="flex flex-col"
        :class="
          panelWidth
            ? 'max-w-[calc(100vw-2rem)]'
            : 'w-max min-w-48 max-w-[min(20rem,calc(100vw-2rem))]'
        "
        :style="panelWidth ? { width: panelWidth } : undefined"
      >
        <div
          v-if="title"
          class="border-b border-gray-100 px-4 pb-2 pt-3 text-sm font-semibold text-gray-900"
        >
          {{ title }}
        </div>
        <div v-if="showSearch" class="border-b border-gray-100 p-3">
          <div
            class="relative flex h-9 items-center rounded-md border border-gray-300 bg-white transition focus-within:border-primary focus-within:ring-1 focus-within:ring-primary"
          >
            <IconSearch
              class="pointer-events-none absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
            />
            <input
              ref="searchInputRef"
              v-model="keyword"
              type="text"
              class="h-full w-full bg-transparent pl-8 pr-3 text-sm text-gray-900 outline-none"
              :placeholder="searchPlaceholder"
              :aria-label="searchPlaceholder"
              role="combobox"
              aria-autocomplete="list"
              aria-expanded="true"
              :aria-haspopup="hasTree ? 'tree' : 'listbox'"
              :aria-controls="listboxId"
              :aria-activedescendant="activeOptionId"
              @keydown="onSearchKeydown"
            />
          </div>
        </div>
        <div
          :id="listboxId"
          ref="listRef"
          class="max-h-96 overflow-y-auto overscroll-contain p-1.5 outline-none"
          :tabindex="showSearch ? -1 : 0"
          :role="hasTree ? 'tree' : 'listbox'"
          :aria-label="label"
          :aria-activedescendant="activeOptionId"
          @keydown="onListKeydown"
          @scroll.passive="onScroll"
        >
          <template v-for="row in visibleRows" :key="row.key">
            <div
              v-if="row.kind === 'group'"
              class="px-2 pb-1 pt-2 text-xs font-medium text-gray-500"
              role="presentation"
            >
              {{ row.label }}
            </div>
            <div
              v-else
              :id="row.id"
              :data-option-id="row.id"
              class="group relative flex min-h-8 w-full cursor-pointer select-none items-center gap-2 rounded py-1.5 pr-2 text-left transition-colors"
              :class="[
                row.option.disabled
                  ? 'cursor-not-allowed opacity-70'
                  : 'cursor-pointer hover:bg-gray-100',
                isRowSelected(row)
                  ? 'bg-gray-100'
                  : activeKey === row.key
                    ? 'bg-gray-50'
                    : '',
              ]"
              :style="{ paddingLeft: `${8 + row.depth * 16}px` }"
              :role="hasTree ? 'treeitem' : 'option'"
              :aria-level="hasTree ? row.depth + 1 : undefined"
              :aria-selected="
                row.isClear || isSelectable(row.option)
                  ? isRowSelected(row)
                  : undefined
              "
              :aria-disabled="row.option.disabled || undefined"
              :aria-expanded="row.hasChildren ? row.expanded : undefined"
              @mouseenter="!row.option.disabled && (activeKey = row.key)"
              @click="selectRow(row)"
            >
              <slot
                name="option"
                :option="row.option"
                :selected="isRowSelected(row)"
                :active="activeKey === row.key"
                :depth="row.depth"
                :expanded="row.expanded"
                :has-children="row.hasChildren"
              >
                <component
                  :is="row.option.icon"
                  v-if="row.option.icon"
                  class="h-4 w-4 shrink-0 text-gray-500"
                />
                <div class="flex min-w-0 flex-1 flex-col">
                  <span
                    class="truncate text-sm"
                    :class="
                      isRowSelected(row)
                        ? 'text-gray-900'
                        : 'text-gray-700 group-hover:text-gray-900'
                    "
                  >
                    {{ row.option.label }}
                  </span>
                  <span
                    v-if="row.option.description"
                    class="truncate text-xs text-gray-500"
                  >
                    {{ row.option.description }}
                  </span>
                </div>
              </slot>
              <span
                v-if="isRowSelected(row)"
                class="ml-auto flex h-5 w-5 shrink-0 items-center justify-center text-primary"
                aria-hidden="true"
              >
                <IconCheck class="h-4 w-4" />
              </span>
              <span
                v-if="row.hasChildren"
                class="flex h-5 w-5 shrink-0 items-center justify-center rounded text-gray-400 transition-colors hover:bg-gray-200 hover:text-gray-600"
                aria-hidden="true"
                @click.stop="toggleRow(row)"
              >
                <IconArrowRight
                  class="h-4 w-4 transition-transform"
                  :class="{ 'rotate-90': row.expanded }"
                />
              </span>
            </div>
          </template>
          <div
            v-if="loading"
            class="flex items-center justify-center py-3 text-gray-400"
          >
            <slot name="loading">
              <VLoading />
            </slot>
          </div>
          <div
            v-else-if="!dataOptionRows.length"
            class="px-3 py-8 text-center text-sm text-gray-500"
          >
            <slot name="empty">{{ emptyText }}</slot>
          </div>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
