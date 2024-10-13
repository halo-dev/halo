<script setup lang="ts">
import type { ListedAuthProvider } from "@halo-dev/api-client";
import { VCard } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { VueDraggable } from "vue-draggable-plus";
import AuthProviderListItem from "./AuthProviderListItem.vue";

const queryClient = useQueryClient();

const modelValue = defineModel<ListedAuthProvider[]>({ default: [] });

const { loading = false } = defineProps<{
  loading?: boolean;
  title: string;
}>();

const emit = defineEmits<{
  (e: "update"): void;
}>();

function onReload() {
  queryClient.invalidateQueries({ queryKey: ["auth-providers"] });
}
</script>
<template>
  <VCard :title="title" :body-class="['!p-0']">
    <VueDraggable
      v-model="modelValue"
      ghost-class="opacity-50"
      handle=".drag-element"
      class="box-border h-full w-full divide-y divide-gray-100"
      :class="{
        'cursor-progress opacity-60': loading,
      }"
      role="list"
      tag="ul"
      :disabled="loading"
      @update="emit('update')"
    >
      <li v-for="authProvider in modelValue" :key="authProvider.name">
        <AuthProviderListItem
          :auth-provider="authProvider"
          @reload="onReload"
        />
      </li>
    </VueDraggable>
  </VCard>
</template>
