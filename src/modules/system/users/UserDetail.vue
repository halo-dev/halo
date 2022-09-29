<script lang="ts" setup>
import { IconUserSettings, VTag } from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject } from "vue";
import { useRouter } from "vue-router";
import type { User } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";

const user = inject<Ref<User>>("user");

const roles = computed(() => {
  return JSON.parse(
    user?.value?.metadata?.annotations?.[rbacAnnotations.ROLE_NAMES] || "[]"
  );
});

const router = useRouter();
</script>
<template>
  <div class="border-t border-gray-100">
    <dl class="divide-y divide-gray-50">
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">显示名称</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.displayName }}
        </dd>
      </div>
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">用户名</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.metadata?.name }}
        </dd>
      </div>
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">电子邮箱</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.email || "未设置" }}
        </dd>
      </div>
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">角色</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          <VTag
            v-for="(role, index) in roles"
            :key="index"
            @click="router.push({ name: 'RoleDetail', params: { name: role } })"
          >
            <template #leftIcon>
              <IconUserSettings />
            </template>
            {{ role }}
          </VTag>
        </dd>
      </div>
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">描述</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.spec?.bio || "无" }}
        </dd>
      </div>
      <div
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">注册时间</dt>
        <dd
          class="mt-1 text-sm tabular-nums text-gray-900 sm:col-span-3 sm:mt-0"
        >
          {{ formatDatetime(user?.metadata?.creationTimestamp) }}
        </dd>
      </div>
      <!-- TODO: add display last login time support -->
      <div
        v-if="false"
        class="bg-white py-5 px-2 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4"
      >
        <dt class="text-sm font-medium text-gray-900">最近登录时间</dt>
        <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
          {{ user?.metadata?.creationTimestamp }}
        </dd>
      </div>
    </dl>
  </div>
</template>
