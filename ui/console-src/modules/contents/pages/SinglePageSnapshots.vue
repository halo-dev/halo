<script setup lang="ts">
import {
  Dialog,
  IconHistoryLine,
  Toast,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRoute } from "vue-router";
import { apiClient } from "@/utils/api-client";
import { computed, watch } from "vue";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { useRouteQuery } from "@vueuse/router";
import SnapshotContent from "./components/SnapshotContent.vue";
import SnapshotListItem from "./components/SnapshotListItem.vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const route = useRoute();
const { t } = useI18n();

const singlePageName = computed(() => route.query.name as string);

const { data: singlePage } = useQuery({
  queryKey: ["singlePage-by-name", singlePageName],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.singlePage.getContentHaloRunV1alpha1SinglePage({
        name: singlePageName.value,
      });
    return data;
  },
  enabled: computed(() => !!singlePageName.value),
});

const { data: snapshots, isLoading } = useQuery({
  queryKey: ["singlePage-snapshots-by-singlePage-name", singlePageName],
  queryFn: async () => {
    const { data } = await apiClient.singlePage.listSinglePageSnapshots({
      name: singlePageName.value,
    });
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (item) => !!item.metadata.deletionTimestamp
    );
    return hasDeletingData ? 1000 : false;
  },
  enabled: computed(() => !!singlePageName.value),
});

const selectedSnapshotName = useRouteQuery<string | undefined>("snapshot-name");

watch(
  () => snapshots.value,
  (value) => {
    if (value && !selectedSnapshotName.value) {
      selectedSnapshotName.value = value[0].metadata.name;
    }

    // Reset selectedSnapshotName if the selected snapshot is deleted
    if (
      !value?.some(
        (snapshot) => snapshot.metadata.name === selectedSnapshotName.value
      )
    ) {
      selectedSnapshotName.value = value?.[0].metadata.name;
    }
  },
  {
    immediate: true,
  }
);

function handleCleanup() {
  Dialog.warning({
    title: t("core.page_snapshots.operations.cleanup.title"),
    description: t("core.page_snapshots.operations.cleanup.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      const { releaseSnapshot, baseSnapshot, headSnapshot } =
        singlePage.value?.spec || {};
      const snapshotsToDelete = snapshots.value?.filter((snapshot) => {
        const { name } = snapshot.metadata;
        return ![releaseSnapshot, baseSnapshot, headSnapshot]
          .filter(Boolean)
          .includes(name);
      });

      if (!snapshotsToDelete?.length) {
        Toast.info(t("core.page_snapshots.operations.cleanup.toast_empty"));
        return;
      }

      for (let i = 0; i < snapshotsToDelete?.length; i++) {
        await apiClient.singlePage.deleteSinglePageContent({
          name: singlePageName.value,
          snapshotName: snapshotsToDelete[i].metadata.name,
        });
      }

      await queryClient.invalidateQueries({
        queryKey: ["singlePage-snapshots-by-singlePage-name", singlePageName],
      });

      Toast.success(t("core.page_snapshots.operations.cleanup.toast_success"));
    },
  });
}
</script>

<template>
  <VPageHeader :title="singlePage?.spec.title">
    <template #icon>
      <IconHistoryLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" @click="$router.back()">
          {{ $t("core.common.buttons.back") }}
        </VButton>
        <VButton size="sm" type="danger" @click="handleCleanup">
          {{ $t("core.page_snapshots.operations.cleanup.button") }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0">
        <div
          class="relative col-span-12 h-full overflow-auto sm:col-span-6 lg:col-span-3 xl:col-span-2"
        >
          <OverlayScrollbarsComponent
            element="div"
            :options="{ scrollbars: { autoHide: 'scroll' } }"
            class="h-full w-full"
            defer
          >
            <VLoading v-if="isLoading" />
            <Transition v-else appear name="fade">
              <ul
                class="box-border h-full w-full divide-y divide-gray-100"
                role="list"
              >
                <li
                  v-for="snapshot in snapshots"
                  :key="snapshot.metadata.name"
                  @click="selectedSnapshotName = snapshot.metadata.name"
                >
                  <SnapshotListItem
                    :snapshot="snapshot"
                    :single-page="singlePage"
                    :selected-snapshot-name="selectedSnapshotName"
                  />
                </li>
              </ul>
            </Transition>
          </OverlayScrollbarsComponent>
        </div>
        <div
          class="col-span-12 h-full overflow-auto sm:col-span-6 lg:col-span-9 xl:col-span-10"
        >
          <SnapshotContent
            :single-page-name="singlePageName"
            :snapshot-name="selectedSnapshotName"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
