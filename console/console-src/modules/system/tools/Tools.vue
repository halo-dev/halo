<script lang="ts" setup>
import { IconMotionLine, VPageHeader } from "@halo-dev/components";
import { computed } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
const routes = computed(() => {
  return router.currentRoute.value.matched[0].children.filter(
    (item) => !!item.meta?.menu
  );
});
</script>

<template>
  <VPageHeader title="工具">
    <template #icon>
      <IconMotionLine class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-4">
    <div
      class="grid grid-cols-1 gap-3 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-5"
    >
      <div
        v-for="item in routes"
        :key="item.name"
        class="group flex min-h-[9rem] cursor-pointer flex-col items-center justify-center space-y-2 rounded-lg border border-dashed bg-white px-4 py-3 shadow transition-all hover:border-solid hover:border-indigo-300"
      >
        <div
          class="inline-flex rounded-full bg-indigo-100 p-2 transition-all group-hover:p-2.5"
        >
          <component
            :is="item.meta?.menu?.icon"
            class="text-xl text-indigo-500 group-hover:text-indigo-700"
          />
        </div>
        <RouterLink
          :to="{ name: item.name }"
          class="line-clamp-1 text-base font-semibold hover:text-gray-600 hover:underline"
        >
          {{ item.meta?.menu?.name }}
        </RouterLink>
      </div>
    </div>
  </div>
</template>
