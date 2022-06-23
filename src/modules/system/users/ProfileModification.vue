<script lang="ts" setup>
import { VButton, VInput, VTextarea } from "@halo-dev/components";
import { inject } from "vue";
import type { User } from "@/types/extension";

const user = inject<User>("user");
</script>
<template>
  <form class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
    <div class="space-y-6 space-y-5 divide-y divide-gray-100">
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          用户名
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput :modelValue="user?.metadata?.name" />
          </div>
        </div>
      </div>
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          显示名称
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput :modelValue="user?.spec?.displayName" />
          </div>
        </div>
      </div>

      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          电子邮箱
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput :modelValue="user?.spec?.email" />
          </div>
        </div>
      </div>
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          个人说明
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VTextarea :modelValue="user?.spec?.bio" />
          </div>
        </div>
      </div>
    </div>

    <div class="pt-5">
      <div class="flex justify-start">
        <VButton type="secondary">保存</VButton>
      </div>
    </div>
  </form>
</template>
