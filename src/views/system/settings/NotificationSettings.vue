<script lang="ts" setup>
import { VInput } from "@/components/base/input";
import { VButton } from "@/components/base/button";
</script>
<template>
  <form class="space-y-8 divide-y divide-gray-200 sm:space-y-5">
    <div class="space-y-6 space-y-5 divide-y divide-gray-100">
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          站点地址
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput modelValue="https://halo.run" />
          </div>
        </div>
      </div>
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          站点标题
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput modelValue="Halo" />
          </div>
        </div>
      </div>
      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          站点副标题
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput modelValue="开源的现代化 CMS 应用" />
          </div>
        </div>
      </div>

      <div class="sm:grid sm:grid-cols-6 sm:items-start sm:gap-4 sm:pt-5">
        <label class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2">
          Logo
        </label>
        <div class="mt-1 sm:col-span-3 sm:mt-0">
          <div class="flex max-w-lg shadow-sm">
            <VInput modelValue="https://halo.run/logo" />
          </div>
        </div>
      </div>
    </div>

    <div class="pt-5">
      <div class="flex justify-start">
        <VButton type="secondary"> 保存</VButton>
      </div>
    </div>
  </form>
</template>
