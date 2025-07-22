<script lang="ts" setup>
import FilterDropdown from "@/components/filter/FilterDropdown.vue";
import { storageAnnotations } from "@/constants/annotations";
import {
  coreApiClient,
  LocalThumbnailStatusPhaseEnum,
} from "@halo-dev/api-client";
import {
  Toast,
  VButton,
  VEmpty,
  VEntityContainer,
  VLoading,
  VModal,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { chunk } from "lodash-es";
import { ref, useTemplateRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import AttachmentThumbnailItem from "./AttachmentThumbnailItem.vue";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const page = ref(1);
const size = ref(20);
const selectedStatus = ref<LocalThumbnailStatusPhaseEnum | undefined>(
  undefined
);

watch(
  () => selectedStatus.value,
  () => {
    page.value = 1;
  }
);

const { t } = useI18n();

const {
  data: thumbnails,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["core:attachments:thumbnails", page, size, selectedStatus],
  queryFn: async () => {
    const fieldSelector: string[] = [];
    if (selectedStatus.value) {
      fieldSelector.push(`status.phase=${selectedStatus.value}`);
    }

    const { data } =
      await coreApiClient.storage.localThumbnail.listLocalThumbnail({
        page: page.value,
        size: size.value,
        fieldSelector,
      });
    return data;
  },
  refetchInterval: (data) => {
    const hasAbnormalData = data?.items?.some(
      (thumbnail) =>
        thumbnail.status.phase !== LocalThumbnailStatusPhaseEnum.Succeeded
    );

    return hasAbnormalData ? 1000 : false;
  },
});

async function handleRetryAllFailed() {
  try {
    const { data } =
      await coreApiClient.storage.localThumbnail.listLocalThumbnail({
        fieldSelector: [`status.phase=${LocalThumbnailStatusPhaseEnum.Failed}`],
      });
    const failedThumbnails = data.items;

    if (!failedThumbnails.length) {
      Toast.info(
        t(
          "core.attachment.thumbnails_modal.operations.retry_all_failed.tips_empty"
        )
      );
      return;
    }

    const chunkedFailedThumbnails = chunk(failedThumbnails, 5);

    for (const chunk of chunkedFailedThumbnails) {
      await Promise.all(
        chunk.map(async (thumbnail) => {
          await coreApiClient.storage.localThumbnail.patchLocalThumbnail({
            name: thumbnail.metadata.name,
            jsonPatchInner: [
              {
                op: "add",
                path: "/status/phase",
                value: LocalThumbnailStatusPhaseEnum.Pending,
              },
              {
                op: "add",
                path: "/metadata/annotations",
                value: {
                  [storageAnnotations.RETRY_TIMESTAMP]: Date.now().toString(),
                },
              },
            ],
          });
        })
      );
    }

    Toast.success(
      t(
        "core.attachment.thumbnails_modal.operations.retry_all_failed.tips_success"
      )
    );
    await refetch();
  } catch (error) {
    console.error("Failed to retry all failed thumbnails", error);
  }
}
</script>
<template>
  <VModal
    ref="modal"
    :centered="false"
    :width="1000"
    :title="$t('core.attachment.thumbnails_modal.title')"
    :layer-closable="true"
    @close="emit('close')"
  >
    <div>
      <div class="mb-4 flex items-center justify-between">
        <VSpace spacing="lg" class="flex-wrap">
          <FilterDropdown
            v-model="selectedStatus"
            :items="[
              {
                label: $t('core.common.filters.item_labels.all'),
                value: undefined,
              },
              {
                label: $t('core.attachment.thumbnails.phase.pending'),
                value: LocalThumbnailStatusPhaseEnum.Pending,
              },
              {
                label: $t('core.attachment.thumbnails.phase.succeeded'),
                value: LocalThumbnailStatusPhaseEnum.Succeeded,
              },
              {
                label: $t('core.attachment.thumbnails.phase.failed'),
                value: LocalThumbnailStatusPhaseEnum.Failed,
              },
            ]"
            :label="$t('core.common.filters.labels.status')"
          />
        </VSpace>
        <VButton size="sm" @click="handleRetryAllFailed">
          {{
            $t(
              "core.attachment.thumbnails_modal.operations.retry_all_failed.button"
            )
          }}
        </VButton>
      </div>
      <VLoading v-if="isLoading" />
      <VEmpty
        v-else-if="!thumbnails?.items?.length"
        :title="$t('core.attachment.thumbnails_modal.empty.title')"
        :message="$t('core.attachment.thumbnails_modal.empty.message')"
      >
        <template #actions>
          <VButton @click="refetch">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
        </template>
      </VEmpty>
      <div v-else class="overflow-hidden rounded-base border">
        <VEntityContainer>
          <AttachmentThumbnailItem
            v-for="thumbnail in thumbnails.items"
            :key="thumbnail.metadata.name"
            :thumbnail="thumbnail"
          />
        </VEntityContainer>
      </div>

      <div class="mt-4">
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', {
              total: thumbnails?.total || 0,
            })
          "
          :total="thumbnails?.total || 0"
          :size-options="[20, 30, 50, 100]"
        />
      </div>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
