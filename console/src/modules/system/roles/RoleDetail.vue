<script lang="ts" setup>
import {
  IconArrowRight,
  IconShieldUser,
  VButton,
  VCard,
  VPageHeader,
  VTabbar,
  VTag,
  VAvatar,
  VAlert,
} from "@halo-dev/components";
import { useRoute } from "vue-router";
import { computed, onMounted, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import { pluginLabels, roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import {
  useRoleForm,
  useRoleTemplateSelection,
} from "@/modules/system/roles/composables/use-role";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
import { SUPER_ROLE_NAME } from "@/constants/constants";

const route = useRoute();

const tabActiveId = ref("detail");

const { roleTemplateGroups, handleRoleTemplateSelect, selectedRoleTemplates } =
  useRoleTemplateSelection();

const { formState, saving, handleCreateOrUpdate } = useRoleForm();

const { users } = useUserFetch({ fetchOnMounted: false });

const isSystemReserved = computed(() => {
  return (
    formState.value.metadata.labels?.[roleLabels.SYSTEM_RESERVED] === "true"
  );
});

const isSuperRole = computed(() => {
  return formState.value.metadata.name === SUPER_ROLE_NAME;
});

const getRoleCountText = computed(() => {
  if (formState.value.metadata.name === SUPER_ROLE_NAME) {
    return `包含所有权限`;
  }

  const dependenciesCount = JSON.parse(
    formState.value.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] || "[]"
  ).length;

  return `包含 ${dependenciesCount} 个权限`;
});

watch(
  () => selectedRoleTemplates.value,
  (newValue) => {
    if (formState.value.metadata.annotations) {
      formState.value.metadata.annotations[rbacAnnotations.DEPENDENCIES] =
        JSON.stringify(Array.from(newValue));
    }
  }
);

const handleFetchRole = async () => {
  try {
    const response = await apiClient.extension.role.getv1alpha1Role({
      name: route.params.name as string,
    });
    formState.value = response.data;
    selectedRoleTemplates.value = new Set(
      JSON.parse(
        response.data.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] ||
          "[]"
      )
    );
  } catch (error) {
    console.error(error);
  }
};

const handleUpdateRole = async () => {
  await handleCreateOrUpdate();
  await handleFetchRole();
};

onMounted(() => {
  handleFetchRole();
});
</script>
<template>
  <VPageHeader
    :title="`角色：${
      formState.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
      formState.metadata?.name
    }`"
  >
    <template #icon>
      <IconShieldUser class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="tabActiveId"
          :items="[
            { id: 'detail', label: '详情' },
            { id: 'permissions', label: '权限设置' },
          ]"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div v-if="tabActiveId === 'detail'">
        <div class="px-4 py-4 sm:px-6">
          <h3 class="text-lg font-medium leading-6 text-gray-900">权限信息</h3>
          <p
            class="mt-1 flex max-w-2xl items-center gap-2 text-sm text-gray-500"
          >
            <span>{{ getRoleCountText }}</span>
          </p>
        </div>
        <div class="border-t border-gray-200">
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">名称</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{
                  formState.metadata?.annotations?.[
                    rbacAnnotations.DISPLAY_NAME
                  ] || formState.metadata?.name
                }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">别名</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                {{ formState.metadata?.name }}
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">类型</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <VTag> {{ isSystemReserved ? "系统保留" : "自定义" }} </VTag>
              </dd>
            </div>
            <div
              v-if="false"
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
                {{ formState.metadata?.creationTimestamp }}
              </dd>
            </div>
            <!-- TODO: 支持通过当前角色查询用户 -->
            <div
              v-if="false"
              class="bg-gray-50 px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">用户</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <div
                  class="h-96 overflow-y-auto overflow-x-hidden rounded-sm bg-white shadow-sm transition-all hover:shadow"
                >
                  <ul class="divide-y divide-gray-100" role="list">
                    <RouterLink
                      v-for="(user, index) in users"
                      :key="index"
                      :to="{
                        name: 'UserDetail',
                        params: { name: user.metadata.name },
                      }"
                    >
                      <li class="block cursor-pointer hover:bg-gray-50">
                        <div class="flex items-center px-4 py-4">
                          <div class="flex min-w-0 flex-1 items-center">
                            <div class="flex flex-shrink-0 items-center">
                              <VAvatar
                                :alt="user.spec.displayName"
                                :src="user.spec.avatar"
                                size="md"
                              />
                            </div>
                            <div
                              class="min-w-0 flex-1 px-4 md:grid md:grid-cols-2 md:gap-4"
                            >
                              <div>
                                <p
                                  class="truncate text-sm font-medium text-gray-900"
                                >
                                  {{ user.spec.displayName }}
                                </p>
                                <p class="mt-2 flex items-center">
                                  <span class="text-xs text-gray-500">
                                    {{ user.metadata.name }}
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
                    </RouterLink>
                  </ul>
                </div>
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <div v-if="tabActiveId === 'permissions'">
        <div v-if="isSystemReserved" class="px-4 py-5">
          <VAlert
            title="提示"
            description="系统保留的角色不支持修改，推荐基于此角色创建一个新的角色。"
            class="w-full sm:w-1/4"
          />
        </div>

        <div>
          <dl class="divide-y divide-gray-100">
            <div
              v-for="(group, groupIndex) in roleTemplateGroups"
              :key="groupIndex"
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                <div>
                  {{ $t(`rbac.${group.module}`, group.module as string) }}
                </div>
                <div
                  v-if="
                    group.roles.length &&
                    group.roles[0].metadata.labels?.[pluginLabels.NAME]
                  "
                  class="mt-3 text-xs text-gray-500"
                >
                  由
                  <RouterLink
                    :to="{
                      name: 'PluginDetail',
                      params: {
                        name: group.roles[0].metadata.labels?.[
                          pluginLabels.NAME
                        ],
                      },
                    }"
                    class="hover:text-blue-600"
                  >
                    {{ group.roles[0].metadata.labels?.[pluginLabels.NAME] }}
                  </RouterLink>
                  插件提供
                </div>
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li v-for="(role, index) in group.roles" :key="index">
                    <label
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded-base border p-5 hover:border-primary"
                    >
                      <input
                        v-if="!isSuperRole"
                        v-model="selectedRoleTemplates"
                        :value="role.metadata.name"
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                        :disabled="isSystemReserved"
                        @change="handleRoleTemplateSelect"
                      />
                      <input
                        v-else
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                        checked
                        disabled
                      />
                      <div class="flex flex-1 flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          {{
                            $t(
                              `rbac.${
                                role.metadata.annotations?.[
                                  rbacAnnotations.DISPLAY_NAME
                                ]
                              }`,
                              role.metadata.annotations?.[
                                rbacAnnotations.DISPLAY_NAME
                              ] as string
                            )
                          }}
                        </span>
                        <span
                          v-if="
                            role.metadata.annotations?.[
                              rbacAnnotations.DEPENDENCIES
                            ]
                          "
                          class="text-xs text-gray-400"
                        >
                          依赖于
                          {{
                            JSON.parse(
                              role.metadata.annotations?.[
                                rbacAnnotations.DEPENDENCIES
                              ]
                            )
                              .map((item: string) =>
                                $t(`rbac.${item}`, item as string)
                              )
                              .join("，")
                          }}
                        </span>
                      </div>
                    </label>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
          <div v-permission="['system:roles:manage']" class="p-4">
            <VButton
              :loading="saving"
              type="secondary"
              :disabled="isSystemReserved"
              @click="handleUpdateRole"
            >
              保存
            </VButton>
          </div>
        </div>
      </div>
    </VCard>
  </div>
</template>
