<script lang="ts" setup>
import {
  IconArrowRight,
  IconGitBranch,
  IconShieldUser,
  VButton,
  VCard,
  VPageHeader,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { useRoute, useRouter } from "vue-router";
import { roles } from "@/modules/system/roles/roles-mock";
import { ref } from "vue";
import { users } from "@/modules/system/users/users-mock";
import { Starport } from "vue-starport";

const route = useRoute();

const role = ref();
const roleActiveId = ref("detail");

if (route.params.id) {
  role.value = roles.find((r) => r.id === Number(route.params.id));
} else {
  role.value = roles[0];
}

const router = useRouter();

const handleRouteToUser = (username: string) => {
  router.push({ name: "UserDetail", params: { username } });
};
</script>
<template>
  <VPageHeader :title="`角色：${role.name}`">
    <template #icon>
      <IconShieldUser class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary">
        <template #icon>
          <IconGitBranch class="h-full w-full" />
        </template>
        Fork
      </VButton>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="roleActiveId"
          :items="[
            { id: 'detail', label: '详情' },
            { id: 'permissions', label: '权限设置' },
          ]"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div v-if="roleActiveId === 'detail'">
        <div class="px-4 py-4 sm:px-6">
          <h3 class="text-lg font-medium leading-6 text-gray-900">权限信息</h3>
          <p
            class="mt-1 flex max-w-2xl items-center gap-2 text-sm text-gray-500"
          >
            <span>包含 {{ role.permissions }} 个权限</span>
          </p>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">名称</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ role.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">别名</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ role.slug }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">类型</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <VTag>系统保留</VTag>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">描述</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                超级管理员
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">创建时间</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                2020-01-01
              </dd>
            </div>
            <div
              class="bg-gray-50 px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">用户</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <div
                  class="h-96 overflow-y-auto overflow-x-hidden rounded-sm bg-white shadow-sm transition-all hover:shadow"
                >
                  <ul class="divide-y divide-gray-100" role="list">
                    <li
                      v-for="(user, index) in users"
                      :key="index"
                      class="block cursor-pointer hover:bg-gray-50"
                      @click="handleRouteToUser(user.username)"
                    >
                      <div class="flex items-center px-4 py-4">
                        <div class="flex min-w-0 flex-1 items-center">
                          <div class="flex-shrink-0">
                            <Starport
                              :duration="400"
                              :port="`user-profile-${user.name}`"
                              class="h-12 w-12"
                            >
                              <div
                                class="overflow-hidden rounded border bg-white hover:shadow-sm"
                              >
                                <img
                                  :alt="user.name"
                                  :src="user.avatar"
                                  class="h-full w-full"
                                />
                              </div>
                            </Starport>
                          </div>
                          <div
                            class="min-w-0 flex-1 px-4 md:grid md:grid-cols-2 md:gap-4"
                          >
                            <div>
                              <p
                                class="truncate text-sm font-medium text-gray-900"
                              >
                                {{ user.name }}
                              </p>
                              <p class="mt-2 flex items-center">
                                <span class="text-xs text-gray-500">
                                  {{ user.username }}
                                </span>
                              </p>
                            </div>
                          </div>
                        </div>
                        <div>
                          <IconArrowRight />
                        </div>
                      </div>
                    </li>
                  </ul>
                </div>
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <div v-if="roleActiveId === 'permissions'">
        <div>
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Posts Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Posts Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Posts View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Posts View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Categories Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Categories Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Categories View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Categories View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Tags Management</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Tags Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Tags View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Tags View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>

            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Plugins Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Plugins Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Plugins View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Plugins View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt
                class="flex flex-col gap-y-3 text-sm font-medium text-gray-900"
              >
                <span> Discussions Management </span>
                <span class="text-xs text-gray-400"> 由社区插件提供 </span>
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Discussions Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Discussions View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-themeable-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Discussions View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
          <div class="p-4">
            <VButton type="secondary">保存</VButton>
          </div>
        </div>
      </div>
    </VCard>
  </div>
</template>
