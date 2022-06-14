<script lang="ts" setup>
import type { PropType } from "vue";
import { computed } from "vue";
import type { Direction, Type } from "./interface";

const props = defineProps({
  activeId: {
    type: [Number, String],
  },
  items: {
    type: Object as PropType<Array<Record<string, string>>>,
  },
  type: {
    type: String as PropType<Type>,
    default: "default",
  },
  direction: {
    type: String as PropType<Direction>,
    default: "row",
  },
  idKey: {
    type: String,
    default: "id",
  },
  labelKey: {
    type: String,
    default: "label",
  },
});

const emit = defineEmits(["update:activeId", "change"]);

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
    @apply flex;
    @apply items-center;
    @apply flex-row;
  }

  .tabbar-item {
    @apply flex;
    @apply cursor-pointer;
    @apply self-center;
    @apply transition-all;
    @apply text-base;
    @apply justify-center;
    @apply gap-2;

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
      @apply h-10;
      @apply px-5;
      @apply py-1;
      @apply border-b-gray-100;

      border-bottom-width: 2px;

      &.tabbar-item-active {
        color: #0e1731;
        border-bottom-color: #0e1731;
      }
    }
  }

  &.tabbar-pills {
    .tabbar-items {
      @apply gap-1;
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply h-10;
      @apply px-9;
      @apply py-1;
      @apply opacity-70;
      border-radius: 4px;

      &.tabbar-item-active {
        @apply bg-gray-100;
        @apply opacity-100;
      }

      &:hover {
        @apply bg-gray-100;
      }
    }
  }

  &.tabbar-outline {
    @apply p-1;
    @apply bg-gray-100;
    border-radius: 4px;

    .tabbar-items {
      @apply gap-1;
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply h-10;
      @apply px-9;
      @apply py-1;
      @apply opacity-70;
      border-radius: 4px;

      &.tabbar-item-active {
        @apply bg-white;
        @apply opacity-100;
        @apply shadow-sm;
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
          border-right-color: #0e1731;
        }
      }
    }
  }
}
</style>
