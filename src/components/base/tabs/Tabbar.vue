<script lang="ts" setup>
import type { PropType } from "vue";
import { computed } from "vue";
import type { Type } from "./interface";

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
  return [`tabbar-${props.type}`];
});

const handleChange = (id: number | string) => {
  emit("update:activeId", id);
  emit("change", id);
};
</script>
<template>
  <div class="tabbar-wrapper" :class="classes">
    <div class="tabbar-items">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="tabbar-item"
        :class="{ 'tabbar-item-active': item[idKey] === activeId }"
        @click="handleChange(item[idKey])"
      >
        <div v-if="item.icon" class="tabbar-item-icon">
          <component :is="item.icon" />
        </div>
        <div class="tabbar-item-label">
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
  }

  .tabbar-item {
    @apply flex;
    @apply cursor-pointer;
    @apply self-center;
    @apply transition-all;
    @apply text-base;
    @apply w-full;
    @apply justify-center;

    .tabbar-item-label,
    .tabbar-item-icon {
      @apply self-center;
    }

    .tabbar-item-icon {
      @apply mr-2;
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
}
</style>
