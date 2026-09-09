<script lang="ts" setup>
import type {
  ThemeTemplateFile,
  ThemeTemplateUsage,
} from "@halo-dev/api-client";
import { VButton, VEmpty, VModal, VTabbar, VTag } from "@halo-dev/components";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import {
  createTemplateDescriptions,
  ERROR_TEMPLATE_PATH,
  filterTemplatesByUsageType,
  getCustomTemplateContentTypeLabelKey,
  getUsageTypeLabelKey,
  matchesTemplateKeyword,
  summarizeTemplates,
  type TemplateUsageType,
} from "../../utils/template-capabilities";

const props = defineProps<{
  templates: ThemeTemplateFile[];
  routes?: Record<string, string>;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

type CategoryFilter = "all" | TemplateUsageType;

const activeCategory = ref<CategoryFilter>("system");
const keyword = ref("");

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

// The single source for semantic descriptions and public route patterns,
// keyed by template path. Recomputed on locale switch and route rule changes.
const templateDescriptions = computed(() =>
  createTemplateDescriptions(t, props.routes)
);

function getKnownTemplate(file: ThemeTemplateFile) {
  const known = templateDescriptions.value[file.path];
  if (known) {
    return known;
  }
  // Error sub-templates (error/404.html, ...) share the error page
  // description; a plain fragments file under error/ does not qualify.
  if (
    file.usages.some(
      (usage) => usage.type === "system" && usage.contentType === "error"
    )
  ) {
    return templateDescriptions.value[ERROR_TEMPLATE_PATH];
  }
  return undefined;
}

const categoryTabs = computed(() => [
  { id: "all", label: t("core.common.text.all") },
  ...summarizeTemplates(props.templates)
    // The default tab must always exist even when a theme ships no system
    // templates at all.
    .filter((summary) => summary.type === "system" || summary.total > 0)
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
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  return templates.filter(
    (file) =>
      matchesTemplateKeyword(file, keyword.value) ||
      (normalizedKeyword !== "" &&
        getFileTitle(file).toLowerCase().includes(normalizedKeyword))
  );
});

interface TemplateListItem {
  file: ThemeTemplateFile;
  title: string;
  route?: string;
}

const filteredTemplateItems = computed<TemplateListItem[]>(() =>
  filteredTemplates.value.map((file) => ({
    file,
    title: getFileTitle(file),
    route: getKnownTemplate(file)?.route,
  }))
);

function getFileTitle(file: ThemeTemplateFile): string {
  const known = getKnownTemplate(file);
  if (known) {
    return known.description;
  }
  const customUsage = file.usages.find((usage) => usage.type === "custom");
  if (customUsage?.name) {
    return customUsage.name;
  }
  if (file.usages.some((usage) => usage.type === "layout")) {
    return t("core.theme.templates.usage.layout");
  }
  return file.path;
}

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
      <VEmpty
        v-if="!filteredTemplateItems.length"
        :title="$t('core.theme.templates.modal.empty')"
      />
      <ul v-else class="divide-y divide-gray-100">
        <li
          v-for="item in filteredTemplateItems"
          :key="item.file.path"
          class="py-3"
        >
          <div class="flex flex-wrap items-baseline gap-x-2">
            <span class="text-sm font-medium text-gray-900">
              {{ item.title }}
            </span>
            <span
              v-if="item.route"
              class="break-all font-mono text-xs text-gray-500"
            >
              {{ item.route }}
            </span>
          </div>
          <p
            v-if="item.title !== item.file.path"
            class="mt-0.5 break-all font-mono text-xs text-gray-500"
          >
            {{ item.file.path }}
          </p>
          <div class="mt-1.5 flex flex-wrap items-center gap-2">
            <template v-for="(usage, index) in item.file.usages" :key="index">
              <VTag>{{ getUsageLabel(usage) }}</VTag>
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
