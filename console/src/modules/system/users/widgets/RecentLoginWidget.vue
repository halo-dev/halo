<script lang="ts" setup>
import { VCard, VAvatar } from "@halo-dev/components";
import { useUserFetch } from "@/modules/system/users/composables/use-user";

const { users } = useUserFetch({ fetchOnMounted: true });
</script>
<template>
  <VCard
    :body-class="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近登录"
  >
    <div class="h-full">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(user, index) in users"
          :key="index"
          class="cursor-pointer p-4 hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="flex flex-shrink-0 items-center">
              <VAvatar
                :alt="user.spec.displayName"
                :src="user.spec.avatar"
                size="md"
              />
            </div>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-gray-900">
                {{ user.spec.displayName }}
              </p>
              <p class="truncate text-sm text-gray-500">
                @{{ user.metadata.name }}
              </p>
            </div>
            <div>
              <time class="text-sm text-gray-500">
                {{ user.status?.lastLoginAt }}
              </time>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </VCard>
</template>
