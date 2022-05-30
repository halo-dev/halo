<script lang="ts" setup>
import { VInput } from "@/components/base/input";
import { VTextarea } from "@/components/base/textarea";
import { VButton } from "@/components/base/button";
import { inject } from "vue";

const user = inject("user");
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
            用户名
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput :modelValue="user.username" />
            </div>
          </div>
        </div>
        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            显示名称
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput :modelValue="user.name" />
            </div>
          </div>
        </div>

        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            电子邮箱
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VInput :modelValue="user.email" />
            </div>
          </div>
        </div>
        <div
          class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:border-t sm:border-gray-200 sm:pt-5"
        >
          <label
            class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
          >
            个人说明
          </label>
          <div class="mt-1 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-lg shadow-sm">
              <VTextarea modelValue="Halo" />
            </div>
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
