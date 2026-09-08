<script lang="ts" setup>
import type {
  ThemeTemplateFile,
  ThemeTemplateUsage,
} from "@halo-dev/api-client";
import {
  VButton,
  VEmpty,
  VModal,
  VStatusDot,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import {
  filterTemplatesByUsageType,
  getCustomTemplateContentTypeLabelKey,
  getTemplateStateDotState,
  getTemplateStateLabelKey,
  getUsageTypeLabelKey,
  matchesTemplateKeyword,
  summarizeTemplates,
  type TemplateUsageType,
} from "../../utils/template-capabilities";

const props = defineProps<{
  templates: ThemeTemplateFile[];
  complete: boolean;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

type CategoryFilter = "all" | TemplateUsageType;

const activeCategory = ref<CategoryFilter>("all");
const keyword = ref("");

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const categoryTabs = computed(() => [
  { id: "all", label: t("core.common.text.all") },
  ...summarizeTemplates(props.templates)
    .filter((summary) => summary.total > 0)
    .map((summary) => ({
      id: summary.type,
      label: `${t(getUsageTypeLabelKey(summary.type))} (${summary.total})`,
    })),
]);

const filteredTemplates = computed(() => {
  const templates =
    activeCategory.value === "all"
      ? props.templates
      : filterTemplatesByUsageType(props.templates, activeCategory.value);
  return templates.filter((file) =>
    matchesTemplateKeyword(file, keyword.value)
  );
});

function getUsageLabel(usage: ThemeTemplateUsage): string {
  const typeLabel = t(getUsageTypeLabelKey(usage.type));
  if (usage.type === "custom") {
    const contentTypeLabelKey = getCustomTemplateContentTypeLabelKey(
      usage.contentType
    );
    const contentTypeLabel = contentTypeLabelKey
      ? t(contentTypeLabelKey)
      : usage.contentType;
    return contentTypeLabel ? `${typeLabel} · ${contentTypeLabel}` : typeLabel;
  }
  if (usage.type === "system" && usage.contentType) {
    return `${typeLabel} · ${usage.contentType}`;
  }
  return typeLabel;
}
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.theme.templates.modal.title')"
    :aria-label="$t('core.theme.templates.modal.title')"
    :width="768"
    mount-to-body
    layer-closable
    :centered="false"
    @close="emit('close')"
  >
    <div class="flex flex-col gap-3">
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div class="max-w-full overflow-x-auto">
          <VTabbar
            v-model:active-id="activeCategory"
            :items="categoryTabs"
            type="outline"
          />
        </div>
        <SearchInput
          v-model="keyword"
          sync
          :placeholder="$t('core.theme.templates.modal.search_placeholder')"
        />
      </div>
      <p v-if="!complete" class="text-xs text-yellow-600">
        {{ $t("core.theme.templates.incomplete_notice") }}
      </p>
      <VEmpty
        v-if="!filteredTemplates.length"
        :title="$t('core.theme.templates.modal.empty')"
      />
      <ul v-else class="divide-y divide-gray-100">
        <li v-for="file in filteredTemplates" :key="file.path" class="py-3">
          <div class="flex items-center gap-2">
            <VStatusDot
              v-tooltip="$t(getTemplateStateLabelKey(file.state))"
              :state="getTemplateStateDotState(file.state)"
            />
            <span class="break-all font-mono text-sm text-gray-900">
              {{ file.path }}
            </span>
          </div>
          <div class="mt-1.5 flex flex-wrap items-center gap-2 pl-4">
            <template v-for="(usage, index) in file.usages" :key="index">
              <VTag>{{ getUsageLabel(usage) }}</VTag>
              <span v-if="usage.name" class="text-xs text-gray-900">
                {{ usage.name }}
              </span>
              <span v-if="usage.description" class="text-xs text-gray-500">
                {{ usage.description }}
              </span>
            </template>
          </div>
        </li>
      </ul>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">{{
        $t("core.common.buttons.close")
      }}</VButton>
    </template>
  </VModal>
</template>
