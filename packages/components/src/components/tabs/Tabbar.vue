<script lang="ts" setup>
import { computed } from "vue";
import type { Direction, Type } from "./interface";

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    items?: Array<Record<string, string>>;
    type?: Type;
    direction?: Direction;
    idKey?: string;
    labelKey?: string;
  }>(),
  {
    type: "default",
    direction: "row",
    idKey: "id",
    labelKey: "label",
  }
);

const emit = defineEmits<{
  (event: "update:activeId", value: number | string): void;
  (event: "change", value: number | string): void;
}>();

const classes = computed(() => {
  return [`tabbar-${props.type}`, `tabbar-direction-${props.direction}`];
});

const handleChange = (id: number | string) => {
  emit("update:activeId", id);
  emit("change", id);
};
</script>
<template>
  <div :class="classes" class="tabbar-wrapper">
    <div class="tabbar-items">
      <div
        v-for="(item, index) in items"
        :key="index"
        :class="{ 'tabbar-item-active': item[idKey] === activeId }"
        class="tabbar-item"
        @click="handleChange(item[idKey])"
      >
        <div v-if="item.icon" class="tabbar-item-icon">
          <component :is="item.icon" />
        </div>
        <div v-if="item[labelKey]" class="tabbar-item-label">
          {{ item[labelKey] }}
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss">
.tabbar-wrapper {
  .tabbar-items {
    @apply flex
    items-center
    flex-row;
  }

  .tabbar-item {
    @apply flex
    cursor-pointer
    self-center
    transition-all
    text-base
    justify-center
    gap-2;

    .tabbar-item-label,
    .tabbar-item-icon {
      @apply self-center;
    }
  }

  &.tabbar-default {
    border-bottom-width: 2px;
    @apply border-b-gray-100;

    .tabbar-items {
      margin-bottom: -2px;
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply h-10
      px-5
      py-1
      border-b-gray-100;

      border-bottom-width: 2px;

      &.tabbar-item-active {
        @apply text-secondary
        border-b-secondary;
      }
    }
  }

  &.tabbar-pills {
    .tabbar-items {
      @apply gap-1;
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply h-10
      px-9
      py-1
      opacity-70
      rounded-base;

      &.tabbar-item-active {
        @apply bg-gray-100
        opacity-100;
      }

      &:hover {
        @apply bg-gray-100;
      }
    }
  }

  &.tabbar-outline {
    @apply p-1
    bg-gray-100
    rounded-base;

    .tabbar-items {
      @apply gap-1
      justify-start;
    }

    .tabbar-item {
      @apply h-10
      px-9
      py-1
      opacity-70
      rounded-base;

      &.tabbar-item-active {
        @apply bg-white
        opacity-100
        shadow-sm;
      }

      &:hover {
        @apply bg-white;
      }
    }
  }

  &.tabbar-direction-row {
    .tabbar-items {
      @apply flex-row;
    }
  }

  &.tabbar-direction-column {
    .tabbar-items {
      @apply flex-col;
    }

    &.tabbar-default {
      border-bottom-width: 0;
      @apply border-b-0;
      border-right-width: 2px;
      @apply border-r-gray-100;

      .tabbar-items {
        margin-bottom: 0;
        margin-right: -2px;
      }

      .tabbar-item {
        border-bottom-width: 0;
        border-right-width: 2px;

        &.tabbar-item-active {
          @apply border-r-secondary;
        }
      }
    }
  }
}
</style>
