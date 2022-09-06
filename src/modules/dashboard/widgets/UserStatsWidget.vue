<script lang="ts" name="UserStatsWidget" setup>
import { VCard } from "@halo-dev/components";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
const { users } = useUserFetch({ fetchOnMounted: true });
</script>
<template>
  <VCard class="h-full">
    <dt class="truncate text-sm font-medium text-gray-500">用户</dt>
    <dd class="mt-1 text-3xl font-semibold text-gray-900">
      {{ users.length }}
    </dd>
  </VCard>
</template>
