<script lang="ts" setup>
import { VSwitch, VTag } from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject } from "vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin, Role } from "@halo-dev/api-client";
import { pluginLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import { usePluginLifeCycle } from "./composables/use-plugin";
import { formatDatetime } from "@/utils/date";
import { useQuery } from "@tanstack/vue-query";

const plugin = inject<Ref<Plugin | undefined>>("plugin");
const { changeStatus, isStarted } = usePluginLifeCycle(plugin);

interface RoleTemplateGroup {
  module: string | null | undefined;
  roles: Role[];
}

const { data: pluginRoleTemplates } = useQuery({
  queryKey: ["plugin-roles", plugin?.value?.metadata.name],
  queryFn: async () => {
    const { data } = await apiClient.extension.role.listv1alpha1Role({
      page: 0,
      size: 0,
      labelSelector: [`${pluginLabels.NAME}=${plugin?.value?.metadata.name}`],
    });

    return data.items;
  },
});

const pluginRoleTemplateGroups = computed<RoleTemplateGroup[]>(() => {
  const groups: RoleTemplateGroup[] = [];
  pluginRoleTemplates.value?.forEach((role) => {
    const group = groups.find(
      (group) =>
        group.module === role.metadata.annotations?.[rbacAnnotations.MODULE]
    );
    if (group) {
      group.roles.push(role);
    } else {
      groups.push({
        module: role.metadata.annotations?.[rbacAnnotations.MODULE],
        roles: [role],
      });
    }
  });
  return groups;
});
</script>

<template>
  <Transition mode="out-in" name="fade">
    <div>
      <div class="flex items-center justify-between bg-white px-4 py-4 sm:px-6">
        <div>
          <h3 class="text-lg font-medium leading-6 text-gray-900">插件信息</h3>
          <p class="mt-1 flex max-w-2xl items-center gap-2">
            <span class="text-sm text-gray-500">{{
              plugin?.spec.version
            }}</span>
            <VTag>
              {{ isStarted ? "已启用" : "未启用" }}
            </VTag>
          </p>
        </div>
        <div v-permission="['system:plugins:manage']">
          <VSwitch :model-value="isStarted" @change="changeStatus" />
        </div>
      </div>
      <div class="border-t border-gray-200">
        <dl class="divide-y divide-gray-100">
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">名称</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              {{ plugin?.spec.displayName }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">描述</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              {{ plugin?.spec.description }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">版本</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              {{ plugin?.spec.version }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">Halo 版本要求</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              {{ plugin?.spec.requires }}
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">提供方</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              <a
                v-if="plugin?.spec.author"
                :href="plugin?.spec.author.website"
                target="_blank"
              >
                {{ plugin?.spec.author.name }}
              </a>
              <span v-else>无</span>
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">协议</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              <ul
                v-if="plugin?.spec.license && plugin?.spec.license.length"
                class="list-inside"
                :class="{ 'list-disc': plugin?.spec.license.length > 1 }"
              >
                <li
                  v-for="(license, index) in plugin.spec.license"
                  :key="index"
                >
                  <a v-if="license.url" :href="license.url" target="_blank">
                    {{ license.name }}
                  </a>
                  <span>
                    {{ license.name }}
                  </span>
                </li>
              </ul>
            </dd>
          </div>
          <!-- TODO add display extensions support -->
          <div
            v-if="false"
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">模型定义</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              <span>无</span>
            </dd>
          </div>
          <div
            :class="`${
              pluginRoleTemplateGroups.length ? 'bg-gray-50' : 'bg-white'
            }`"
            class="px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">权限模板</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-5 sm:mt-0">
              <dl
                v-if="pluginRoleTemplateGroups.length"
                class="divide-y divide-gray-100"
              >
                <div
                  v-for="(group, groupIndex) in pluginRoleTemplateGroups"
                  :key="groupIndex"
                  class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
                >
                  <dt class="text-sm font-medium text-gray-900">
                    {{ group.module }}
                  </dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                    <ul class="space-y-2">
                      <li v-for="(role, index) in group.roles" :key="index">
                        <div
                          class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                        >
                          <div class="inline-flex flex-col gap-y-3">
                            <span class="font-medium text-gray-900">
                              {{
                                role.metadata.annotations?.[
                                  rbacAnnotations.DISPLAY_NAME
                                ]
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
                                ).join(", ")
                              }}
                            </span>
                          </div>
                        </div>
                      </li>
                    </ul>
                  </dd>
                </div>
              </dl>
              <span v-else>无</span>
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">最近一次启动</dt>
            <dd
              class="mt-1 text-sm tabular-nums text-gray-900 sm:col-span-2 sm:mt-0"
            >
              {{ formatDatetime(plugin?.status?.lastStartTime) }}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  </Transition>
</template>
