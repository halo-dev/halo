<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VInput } from "@/components/base/input";
</script>
<template>
  <form class="space-y-8 divide-y divide-gray-200">
    <div class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
      <div class="mt-6 space-y-6 sm:mt-5 sm:space-y-5">
        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            原密码
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput />
            </div>
          </div>
        </div>
        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            新密码
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput />
            </div>
          </div>
        </div>

        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            确认密码
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput />
            </div>
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
