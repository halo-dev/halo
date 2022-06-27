<script lang="ts" setup>
import { IconUserSettings, VTag } from "@halo-dev/components";
import { inject } from "vue";
import { useRouter } from "vue-router";
import type { User } from "@/types/extension";

const user = inject<User>("user");

const router = useRouter();
</script>
<template>
  <div class="border-t border-gray-200">
    <dl class="divide-y divide-gray-100">
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">显示名称</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.displayName }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">用户名</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.metadata?.name }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">电子邮箱</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.email || "未设置" }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">角色</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          <VTag @click="router.push({ name: 'RoleDetail', params: { id: 1 } })">
            <template #leftIcon>
              <IconUserSettings />
            </template>
            {{ user?.metadata?.name }}
          </VTag>
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">描述</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.bio }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">两步验证</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec.twoFactorAuthEnabled ? "开启" : "关闭" }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">注册时间</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.metadata?.creationTimestamp }}
        </dd>
      </div>
      <div
        class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
      >
        <dt class="text-sm font-medium text-gray-900">最近登录时间</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.metadata?.creationTimestamp }}
        </dd>
      </div>
    </dl>
  </div>
</template>
