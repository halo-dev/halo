<script setup lang="ts">
import { useRoleTemplateSelection } from "@/composables/use-role";
import { rbacAnnotations } from "@/constants/annotations";
import { pluginLabels } from "@/constants/labels";
import type { Role } from "@halo-dev/api-client";
import { toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    roleTemplates?: Role[];
  }>(),
  {
    roleTemplates: () => [],
  }
);

const { roleTemplates } = toRefs(props);

const { roleTemplateGroups } = useRoleTemplateSelection(roleTemplates);
</script>
<template>
  <dl
    class="divide-y divide-gray-100 border border-gray-100 rounded-base overflow-hidden"
  >
    <div
      v-for="(group, index) in roleTemplateGroups"
      :key="index"
      class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
    >
      <dt class="text-sm font-medium text-gray-900">
        <div>
          {{ $t(`core.rbac.${group.module}`, group.module as string) }}
        </div>
        <div
          v-if="
            group.roles.length &&
            group.roles[0].metadata.labels?.[pluginLabels.NAME]
          "
          class="mt-3 text-xs text-gray-500"
        >
          <i18n-t keypath="core.role.common.text.provided_by_plugin" tag="div">
            <template #plugin>
              <RouterLink
                :to="{
                  name: 'PluginDetail',
                  params: {
                    name: group.roles[0].metadata.labels?.[pluginLabels.NAME],
                  },
                }"
                class="hover:text-blue-600"
              >
                {{ group.roles[0].metadata.labels?.[pluginLabels.NAME] }}
              </RouterLink>
            </template>
          </i18n-t>
        </div>
      </dt>
      <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
        <ul class="space-y-2">
          <li v-for="role in group.roles" :key="role.metadata.name">
            <label
              class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded-base border p-5 hover:border-primary"
            >
              <input type="checkbox" disabled checked />
              <div class="flex flex-1 flex-col gap-y-3">
                <span class="font-medium text-gray-900">
                  {{
                    $t(
                      `core.rbac.${
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
                    role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES]
                  "
                  class="text-xs text-gray-400"
                >
                  {{
                    $t("core.role.common.text.dependent_on", {
                      roles: JSON.parse(
                        role.metadata.annotations?.[
                          rbacAnnotations.DEPENDENCIES
                        ]
                      )
                        .map((item: string) =>
                          $t(`core.rbac.${item}`, item as string)
                        )
                        .join("，"),
                    })
                  }}
                </span>
              </div>
            </label>
          </li>
        </ul>
      </dd>
    </div>
  </dl>
</template>
