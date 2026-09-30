<script lang="ts" setup>
import { VButton, VEmpty, VModal, VTag } from "@halo-dev/components";
import { computed, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import {
  hasDeclaredCapabilities,
  type RouteDeclaration,
  type UiModuleSummary,
} from "../../utils/ui-capabilities";

const props = defineProps<{
  summary: UiModuleSummary;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

interface CapabilityGroup {
  id: string;
  label: string;
  notice?: string;
  routes?: RouteDeclaration[];
  tags?: string[];
}

const groups = computed<CapabilityGroup[]>(() => {
  const { summary } = props;
  return [
    {
      id: "console-routes",
      label: t("core.theme.ui.modal.groups.console_routes"),
      routes: summary.consoleRoutes,
    },
    {
      id: "uc-routes",
      label: t("core.theme.ui.modal.groups.uc_routes"),
      notice: t("core.theme.ui.modal.uc_notice"),
      routes: summary.ucRoutes,
    },
    {
      id: "components",
      label: t("core.theme.ui.modal.groups.components"),
      tags: summary.components,
    },
    {
      id: "extension-points",
      label: t("core.theme.ui.modal.groups.extension_points"),
      tags: summary.extensionPoints,
    },
    {
      id: "formkit-inputs",
      label: t("core.theme.ui.modal.groups.formkit_inputs"),
      tags: summary.formkitInputs,
    },
  ].filter((group) => Boolean(group.routes?.length || group.tags?.length));
});
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.theme.ui.modal.title')"
    :aria-label="$t('core.theme.ui.modal.title')"
    :width="640"
    mount-to-body
    layer-closable
    :centered="false"
    @close="emit('close')"
  >
    <VEmpty
      v-if="!hasDeclaredCapabilities(summary)"
      :title="$t('core.theme.ui.declared.none')"
    />
    <div v-else class="flex flex-col divide-y divide-gray-100">
      <section v-for="group in groups" :key="group.id" class="py-3 first:pt-0">
        <h4 class="text-sm font-medium text-gray-900">
          {{ group.label }}
          <span class="text-gray-400"
            >({{ (group.routes || group.tags)?.length }})</span
          >
        </h4>
        <p v-if="group.notice" class="mt-1 text-xs text-gray-500">
          {{ group.notice }}
        </p>
        <ul v-if="group.routes" class="mt-2 space-y-1.5">
          <li
            v-for="(route, index) in group.routes"
            :key="index"
            class="flex flex-wrap items-center gap-2"
          >
            <span class="break-all font-mono text-sm text-gray-900">
              {{ route.path }}
            </span>
            <VTag v-if="route.name">{{ route.name }}</VTag>
            <span v-if="route.parentName" class="text-xs text-gray-500">
              {{
                $t("core.theme.ui.modal.route_parent", {
                  parent: route.parentName,
                })
              }}
            </span>
          </li>
        </ul>
        <div v-if="group.tags" class="mt-2 flex flex-wrap gap-2">
          <VTag v-for="tag in group.tags" :key="tag">{{ tag }}</VTag>
        </div>
      </section>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">{{
        $t("core.common.buttons.close")
      }}</VButton>
    </template>
  </VModal>
</template>
