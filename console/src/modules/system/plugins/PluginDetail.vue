<script lang="ts" setup>
import {
  VDescription,
  VDescriptionItem,
  VSwitch,
  VTag,
} from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject } from "vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin, Role } from "@halo-dev/api-client";
import { pluginLabels, roleLabels } from "@/constants/labels";
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
      labelSelector: [
        `${pluginLabels.NAME}=${plugin?.value?.metadata.name}`,
        `${roleLabels.TEMPLATE}=true`,
        "!halo.run/hidden",
      ],
    });

    return data.items;
  },
  cacheTime: 0,
  enabled: computed(() => !!plugin?.value?.metadata.name),
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
          <h3 class="text-lg font-medium leading-6 text-gray-900">
            {{ $t("core.plugin.detail.header.title") }}
          </h3>
          <p class="mt-1 flex max-w-2xl items-center gap-2">
            <span class="text-sm text-gray-500">
              {{ plugin?.spec.version }}
            </span>
            <VTag>
              {{
                isStarted
                  ? $t("core.common.status.activated")
                  : $t("core.common.status.not_activated")
              }}
            </VTag>
          </p>
        </div>
        <div v-permission="['system:plugins:manage']">
          <VSwitch :model-value="isStarted" @change="changeStatus" />
        </div>
      </div>
      <div class="border-t border-gray-200">
        <VDescription>
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.display_name')"
            :content="plugin?.spec.displayName"
          />
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.description')"
            :content="plugin?.spec.description"
          />
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.version')"
            :content="plugin?.spec.version"
          />
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.requires')"
            :content="plugin?.spec.requires"
          />
          <VDescriptionItem :label="$t('core.plugin.detail.fields.author')">
            <a
              v-if="plugin?.spec.author"
              :href="plugin?.spec.author.website"
              target="_blank"
            >
              {{ plugin?.spec.author.name }}
            </a>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem :label="$t('core.plugin.detail.fields.license')">
            <ul
              v-if="plugin?.spec.license && plugin?.spec.license.length"
              class="list-inside"
              :class="{ 'list-disc': plugin?.spec.license.length > 1 }"
            >
              <li v-for="(license, index) in plugin.spec.license" :key="index">
                <a v-if="license.url" :href="license.url" target="_blank">
                  {{ license.name }}
                </a>
                <span>
                  {{ license.name }}
                </span>
              </li>
            </ul>
          </VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.role_templates')"
          >
            <dl
              v-if="pluginRoleTemplateGroups.length"
              class="divide-y divide-gray-100"
            >
              <div
                v-for="(group, groupIndex) in pluginRoleTemplateGroups"
                :key="groupIndex"
                class="rounded bg-gray-50 px-4 py-5 hover:bg-gray-100 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
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
                            {{
                              $t("core.role.common.text.dependent_on", {
                                roles: JSON.parse(
                                  role.metadata.annotations?.[
                                    rbacAnnotations.DEPENDENCIES
                                  ]
                                ).join(", "),
                              })
                            }}
                          </span>
                        </div>
                      </div>
                    </li>
                  </ul>
                </dd>
              </div>
            </dl>
            <span v-else>
              {{ $t("core.common.text.none") }}
            </span>
          </VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.plugin.detail.fields.last_starttime')"
            :content="formatDatetime(plugin?.status?.lastStartTime)"
          />
        </VDescription>
      </div>
    </div>
  </Transition>
</template>
