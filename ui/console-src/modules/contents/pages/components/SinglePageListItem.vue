<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import type { ListedSinglePage, SinglePage } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconExternalLinkLine,
  IconEye,
  IconEyeOff,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import type { Ref } from "vue";
import { computed, inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { RouterLink } from "vue-router";
import ContributorList from "../../_components/ContributorList.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "open-setting-modal", post: SinglePage): void;
}>();

const selectedPageNames = inject<Ref<string[]>>("selectedPageNames", ref([]));

const externalUrl = computed(() => {
  const { metadata, status } = props.singlePage.page;
  if (metadata.labels?.[singlePageLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/singlepages/${metadata.name}`;
});

const publishStatus = computed(() => {
  const { labels } = props.singlePage.page.metadata;
  return labels?.[singlePageLabels.PUBLISHED] === "true"
    ? t("core.page.filters.status.items.published")
    : t("core.page.filters.status.items.draft");
});

const isPublishing = computed(() => {
  const { spec, status, metadata } = props.singlePage.page;
  return (
    (spec.publish &&
      metadata.labels?.[singlePageLabels.PUBLISHED] !== "true") ||
    (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
  );
});

const { mutate: changeVisibleMutation } = useMutation({
  mutationFn: async (singlePage: SinglePage) => {
    const { data } = await coreApiClient.content.singlePage.getSinglePage({
      name: singlePage.metadata.name,
    });
    data.spec.visible = data.spec.visible === "PRIVATE" ? "PUBLIC" : "PRIVATE";
    await coreApiClient.content.singlePage.updateSinglePage(
      {
        name: singlePage.metadata.name,
        singlePage: data,
      },
      {
        mute: true,
      }
    );
    await queryClient.invalidateQueries({ queryKey: ["singlePages"] });
  },
  retry: 3,
  onSuccess: () => {
    Toast.success(t("core.common.toast.operation_success"));
  },
  onError: () => {
    Toast.error(t("core.common.toast.operation_failed"));
  },
});

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.page.operations.delete.title"),
    description: t("core.page.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const singlePageToUpdate = cloneDeep(props.singlePage.page);
      singlePageToUpdate.spec.deleted = true;
      await coreApiClient.content.singlePage.updateSinglePage({
        name: props.singlePage.page.metadata.name,
        singlePage: singlePageToUpdate,
      });
      await queryClient.invalidateQueries({ queryKey: ["singlePages"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:singlepages:manage'])"
      #checkbox
    >
      <input
        v-model="selectedPageNames"
        :value="singlePage.page.metadata.name"
        type="checkbox"
      />
    </template>
    <template #start>
      <VEntityField
        :title="singlePage.page.spec.title"
        :route="{
          name: 'SinglePageEditor',
          query: { name: singlePage.page.metadata.name },
        }"
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
    <template #end>
      <VEntityField>
        <template #description>
          <ContributorList :contributors="singlePage.contributors" />
        </template>
      </VEntityField>
      <VEntityField :description="publishStatus">
        <template v-if="isPublishing" #description>
          <VStatusDot :text="$t('core.common.tooltips.publishing')" animate />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <IconEye
            v-if="singlePage.page.spec.visible === 'PUBLIC'"
            v-tooltip="$t('core.page.filters.visible.items.public')"
            class="cursor-pointer text-sm transition-all hover:text-blue-600"
            @click="changeVisibleMutation(singlePage.page)"
          />
          <IconEyeOff
            v-if="singlePage.page.spec.visible === 'PRIVATE'"
            v-tooltip="$t('core.page.filters.visible.items.private')"
            class="cursor-pointer text-sm transition-all hover:text-blue-600"
            @click="changeVisibleMutation(singlePage.page)"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="singlePage?.page?.spec.deleted">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(singlePage.page.spec.publishTime) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:singlepages:manage'])"
      #dropdownItems
    >
      <VDropdownItem
        @click="
          $router.push({
            name: 'SinglePageEditor',
            query: { name: singlePage.page.metadata.name },
          })
        "
      >
        {{ $t("core.common.buttons.edit") }}
      </VDropdownItem>
      <VDropdownItem @click="emit('open-setting-modal', singlePage.page)">
        {{ $t("core.common.buttons.setting") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleDelete">
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
