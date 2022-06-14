<script lang="ts" setup>
import { VButton, VInput } from "@halo-dev/components";
</script>
<template>
  <form class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
    <div class="space-y-6 space-y-5 divide-y divide-gray-100">
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          原密码
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput />
          </div>
        </div>
      </div>
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          新密码
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput />
          </div>
        </div>
      </div>

      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          确认密码
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput />
          </div>
        </div>
      </div>
    </div>

    <div class="pt-5">
      <div class="flex justify-start">
        <VButton type="secondary">修改密码</VButton>
      </div>
    </div>
  </form>
</template>
