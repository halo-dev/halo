<script lang="ts" setup>
import { VTag } from "@/components/base/tag";
import { IconUserSettings } from "@/core/icons";
import { inject } from "vue";

const user = inject("user");
</script>
<template>
  <div class="border-t border-gray-200">
    <dl class="divide-y divide-gray-100">
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">显示名称</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          {{ user.name }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">用户名</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          {{ user.username }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">电子邮箱</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          {{ user.email || "未设置" }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">角色</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          <VTag>
            <template #leftIcon>
              <IconUserSettings class="h-full w-full" />
            </template>
            {{ user.role }}
          </VTag>
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">描述</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          Hello Halo
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">两步验证</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">已启用</dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">注册时间</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          2022-01-01 00:00:00
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">最近登录时间</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
          2022-05-30 12:00:00
        </dd>
      </div>
    </dl>
  </div>
</template>
