<script lang="ts" setup>
import { VCard } from "@/components/base/card";
import { users } from "@/views/system/users/users-mock";
</script>
<template>
  <VCard
    :bodyClass="['h-full', '!p-0', 'overflow-y-auto']"
    class="h-full"
    title="最近登录"
  >
    <div class="p-4 h-full">
      <ul class="divide-y divide-gray-200" role="list">
        <li
          v-for="(user, index) in users"
          :key="index"
          class="py-4 cursor-pointer hover:bg-gray-50"
        >
          <div class="flex items-center space-x-4">
            <div class="flex-shrink-0">
              <img
                :alt="user.name"
                :src="user.avatar"
                class="h-10 w-10 rounded"
              />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">
                {{ user.name }}
              </p>
              <p class="text-sm text-gray-500 truncate">@{{ user.username }}</p>
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
