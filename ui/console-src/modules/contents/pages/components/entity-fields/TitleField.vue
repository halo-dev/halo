<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import type { ListedSinglePage } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);

const externalUrl = computed(() => {
  const { metadata, status } = props.singlePage.page;
  if (metadata.labels?.[singlePageLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/singlepages/${metadata.name}`;
});
</script>

<template>
  <VEntityField
    :title="singlePage.page.spec.title"
    :route="{
      name: 'SinglePageEditor',
      query: { name: singlePage.page.metadata.name },
    }"
    max-width="30rem"
  >
    <template #extra>
      <VSpace>
        <RouterLink
          v-if="singlePage.page.status?.inProgress"
          v-tooltip="$t('core.common.tooltips.unpublished_content_tip')"
          :to="{
            name: 'SinglePageEditor',
            query: { name: singlePage.page.metadata.name },
          }"
          class="flex items-center"
        >
          <VStatusDot state="success" animate />
        </RouterLink>
        <a
          target="_blank"
          :href="externalUrl"
          class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
        >
          <IconExternalLinkLine class="h-3.5 w-3.5" />
        </a>
      </VSpace>
    </template>
    <template #description>
      <div class="flex w-full flex-col gap-1">
        <VSpace class="w-full">
          <span class="text-xs text-gray-500">
            {{
              $t("core.page.list.fields.visits", {
                visits: singlePage.stats.visit || 0,
              })
            }}
          </span>
          <span class="text-xs text-gray-500">
            {{
              $t("core.page.list.fields.comments", {
                comments: singlePage.stats.totalComment || 0,
              })
            }}
          </span>
        </VSpace>
      </div>
    </template>
  </VEntityField>
</template>
