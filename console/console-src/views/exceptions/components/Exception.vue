<script lang="ts" setup>
import { VButton, VSpace } from "@halo-dev/components";
import { useRouter } from "vue-router";
withDefaults(
  defineProps<{
    code: number | string;
    title?: string;
    message?: string;
  }>(),
  {
    title: undefined,
    message: undefined,
  }
);

const router = useRouter();
</script>

<template>
  <div class="grid h-screen place-content-center bg-white px-4">
    <div class="text-center">
      <h1 class="text-8xl font-black text-gray-200">
        {{ code }}
      </h1>

      <p class="text-2xl font-bold tracking-tight text-gray-900 sm:text-4xl">
        {{ title }}
      </p>

      <p class="mt-4 text-gray-500">{{ message }}</p>

      <div class="mt-4">
        <VSpace>
          <VButton @click="router.back()">
            {{ $t("core.common.buttons.back") }}
          </VButton>
          <VButton type="secondary" :route="{ name: 'Dashboard' }">
            {{ $t("core.exception.actions.dashboard") }}
          </VButton>
        </VSpace>
      </div>
    </div>
  </div>
</template>
