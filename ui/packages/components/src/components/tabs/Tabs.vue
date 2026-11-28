<script lang="ts" setup>
import type { Component, ComputedRef, VNode } from "vue";
import {
  Fragment,
  computed,
  onMounted,
  provide,
  useId,
  useSlots,
  watch,
} from "vue";
import { VTabbar } from "./index";
import type { TabbarItem, TabsDirection, TabsSize, TabsType } from "./types";

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    type?: TabsType;
    direction?: TabsDirection;
    idKey?: string;
    labelKey?: string;
    flush?: boolean;
    size?: TabsSize;
    stretch?: boolean;
    ariaLabel?: string;
    centered?: boolean;
    divider?: boolean;
    background?: boolean;
  }>(),
  {
    activeId: undefined,
    type: "default",
    direction: "row",
    idKey: "id",
    labelKey: "label",
    flush: false,
    size: "md",
    stretch: false,
    ariaLabel: undefined,
    centered: false,
    divider: true,
    background: true,
  }
);

const emit = defineEmits<{
  (event: "update:activeId", value: number | string): void;
  (event: "change", value: number | string): void;
}>();

const slots = useSlots();

function flattenVNodes(nodes: unknown[]): VNode[] {
  const result: VNode[] = [];

  for (const node of nodes) {
    if (!node || typeof node !== "object") {
      continue;
    }

    const vnode = node as VNode;

    if (vnode.type === Fragment) {
      if (Array.isArray(vnode.children)) {
        result.push(...flattenVNodes(vnode.children));
      }
      continue;
    }

    result.push(vnode);
  }

  return result;
}

const tabItems = computed<TabbarItem[]>(() => {
  return flattenVNodes(slots.default?.() ?? [])
    .map((node) => node.props ?? {})
    .map((slotProps) => ({
      id: slotProps[props.idKey] as string | number | undefined,
      label: slotProps[props.labelKey] as string | undefined,
      icon: slotProps.icon as Component | undefined,
      // A bare `disabled` attribute reaches the raw vnode props as "".
      disabled: slotProps.disabled === true || slotProps.disabled === "",
      badge: slotProps.badge as string | number | undefined,
      ariaLabel: slotProps.ariaLabel as string | undefined,
    }))
    .filter(
      (item) =>
        item.id !== undefined &&
        (item.label !== undefined || item.ariaLabel !== undefined)
    );
});

const resolvedActiveId = computed<string | number | undefined>(() => {
  const items = tabItems.value;

  if (!items.length) {
    return props.activeId;
  }

  if (items.some((item) => item.id === props.activeId && !item.disabled)) {
    return props.activeId;
  }

  return items.find((item) => !item.disabled)?.id;
});

provide<ComputedRef<string | number | undefined>>("activeId", resolvedActiveId);

provide<string>("tabsId", useId());

function syncActiveId() {
  const id = resolvedActiveId.value;

  if (id !== undefined && id !== props.activeId) {
    emit("update:activeId", id);
  }
}

onMounted(syncActiveId);

watch(resolvedActiveId, syncActiveId, { flush: "post" });

const classes = computed(() => {
  return [`tabs-direction-${props.direction}`];
});

const handleChange = (id: string | number) => {
  emit("update:activeId", id);
  emit("change", id);
};
</script>
<template>
  <div :class="classes" class="tabs-wrapper">
    <div class="tabs-bar-wrapper">
      <VTabbar
        :active-id="resolvedActiveId"
        :aria-label="ariaLabel"
        :background="background"
        :centered="centered"
        :direction="direction"
        :divider="divider"
        :flush="flush"
        :items="tabItems"
        :size="size"
        :stretch="stretch"
        :type="type"
        @change="handleChange"
      >
        <template v-if="$slots.left" #left>
          <slot name="left" />
        </template>
        <template v-if="$slots.right" #right>
          <slot name="right" />
        </template>
      </VTabbar>
    </div>
    <div class="tabs-items-wrapper">
      <slot />
    </div>
  </div>
</template>
<style lang="scss">
.tabs-wrapper {
  @apply flex;

  &.tabs-direction-row {
    @apply flex-col;

    .tabs-items-wrapper {
      @apply mt-2;
    }
  }

  &.tabs-direction-column {
    .tabs-items-wrapper {
      @apply ml-2;
    }
  }
}
</style>
