<script lang="ts" name="RecentLoginWidget" setup>
import { VCard } from "@halo-dev/components";
import { users } from "@/modules/system/users/users-mock";
</script>
<template>
  <VCard
    :bodyClass="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近登录"
  >
    <div class="h-full p-4">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(user, index) in users"
          :key="index"
          class="cursor-pointer py-4 hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="flex-shrink-0">
              <img
                :alt="user.name"
                :src="user.avatar"
                class="h-10 w-10 rounded"
              />
            </div>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-gray-900">
                {{ user.name }}
              </p>
              <p class="truncate text-sm text-gray-500">@{{ user.username }}</p>
            </div>
            <div>
              <time class="text-sm text-gray-500" datetime="2020-01-07 20:00">
                2020-01-07 20:00
              </time>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </VCard>
</template>
