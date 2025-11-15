<script lang="ts" setup>
import { IconArrowRight } from "@halo-dev/components";
import {
  utils,
  type DashboardWidgetQuickActionItem,
} from "@halo-dev/ui-shared";
import { useRouter } from "vue-router";

const props = defineProps<{
  item: DashboardWidgetQuickActionItem;
}>();

const router = useRouter();

function handleClick() {
  if ("action" in props.item && props.item.action) {
    props.item.action();
  } else if ("route" in props.item && props.item.route) {
    router.push(props.item.route);
  }
}
</script>

<template>
  <template v-if="utils.permission.has(item.permissions || [])">
    <div
      class="group relative cursor-pointer rounded-lg bg-gray-50 p-4 transition-all hover:bg-gray-100"
      @click="handleClick"
    >
      <div>
        <span
          class="inline-flex rounded-lg bg-teal-50 p-3 text-teal-700 ring-4 ring-white"
        >
          <component :is="item.icon"></component>
        </span>
      </div>
      <div class="mt-8">
        <h3 class="text-sm font-semibold">
          {{ item.title }}
        </h3>
      </div>
      <span
        aria-hidden="true"
        class="pointer-events-none absolute right-6 top-6 text-gray-300 transition-all group-hover:translate-x-1 group-hover:text-gray-400"
      >
        <IconArrowRight />
      </span>
    </div>
  </template>
</template>
