<script setup lang="ts">
import {
  type ContentWrapper,
  type ListedSnapshotDto,
} from "@halo-dev/api-client";
import {
  Dialog,
  IconHistoryLine,
  Toast,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, toRefs, watch } from "vue";
import { useI18n } from "vue-i18n";
import SnapshotContent from "./SnapshotContent.vue";
import SnapshotDiffContent from "./SnapshotDiffContent.vue";
import SnapshotListItem from "./SnapshotListItem.vue";
import { SNAPSHOTS_QUERY_KEY } from "./query-keys";

const props = defineProps<{
  title: string;
  name: string;
  cacheKey: string;
  releaseSnapshot?: string;
  baseSnapshot?: string;
  headSnapshot?: string;
  listApi: () => Promise<ListedSnapshotDto[]>;
  getApi: (snapshotName: string) => Promise<ContentWrapper>;
  deleteApi: (snapshotName: string) => Promise<void>;
  revertApi: (snapshotName: string) => Promise<void>;
}>();

const { cacheKey, name } = toRefs(props);

const { t } = useI18n();
const queryClient = useQueryClient();

const { data: snapshots, isLoading } = useQuery({
  queryKey: SNAPSHOTS_QUERY_KEY(cacheKey, name),
  queryFn: async () => {
    return await props.listApi();
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (item) => !!item.metadata.deletionTimestamp
    );
    return hasDeletingData ? 1000 : false;
  },
  enabled: computed(() => !!name.value),
});

const selectedSnapshotNames = useRouteQuery<string[] | undefined>(
  "snapshot-names",
  [],
  {
    transform: (value: string[] | string | undefined) => {
      if (Array.isArray(value)) {
        return value;
      }
      return value ? [value] : [];
    },
  }
);

const diffModeQuery = useRouteQuery<string | undefined>("diff-mode");

const diffMode = computed(() => diffModeQuery.value === "true");

function handleToggleDiffMode() {
  if (diffMode.value) {
    selectedSnapshotNames.value = [selectedSnapshotNames.value?.[0]].filter(
      Boolean
    ) as string[];
  }
  diffModeQuery.value = !diffMode.value ? "true" : undefined;
}

function handleSelectSnapshot(snapshotName: string) {
  if (!diffMode.value) {
    selectedSnapshotNames.value = [snapshotName];
    return;
  }

  // Diff mode
  if (selectedSnapshotNames.value?.includes(snapshotName)) {
    selectedSnapshotNames.value = selectedSnapshotNames.value?.filter(
      (name) => name !== snapshotName
    );
    return;
  }

  if (selectedSnapshotNames.value?.length === 2) {
    selectedSnapshotNames.value = [snapshotName];
    return;
  }

  selectedSnapshotNames.value = [
    ...(selectedSnapshotNames.value || []),
    snapshotName,
  ];

  selectedSnapshotNames.value = selectedSnapshotNames.value?.sort((a, b) => {
    const aIndex = snapshots.value?.findIndex(
      (snapshot) => snapshot.metadata.name === a
    );
    const bIndex = snapshots.value?.findIndex(
      (snapshot) => snapshot.metadata.name === b
    );
    return (aIndex || 0) - (bIndex || 0);
  });
}

watch(
  () => snapshots.value,
  (value) => {
    if (!value) {
      return;
    }

    if (!selectedSnapshotNames.value?.length) {
      selectedSnapshotNames.value = [value[0].metadata.name];
    }

    // Reset selectedSnapshotName if the selected snapshot is deleted
    if (
      !value.some((snapshot) =>
        selectedSnapshotNames.value?.includes(snapshot.metadata.name)
      )
    ) {
      selectedSnapshotNames.value = [value?.[0].metadata.name].filter(
        Boolean
      ) as string[];
    }
  },
  {
    immediate: true,
  }
);

function handleCleanup() {
  Dialog.warning({
    title: t("core.snapshots.operations.cleanup.title"),
    description: t("core.snapshots.operations.cleanup.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    async onConfirm() {
      const snapshotsToDelete = snapshots.value?.filter((snapshot) => {
        const { name } = snapshot.metadata;
        return ![props.releaseSnapshot, props.baseSnapshot, props.headSnapshot]
          .filter(Boolean)
          .includes(name);
      });

      if (!snapshotsToDelete?.length) {
        Toast.info(t("core.snapshots.operations.cleanup.toast_empty"));
        return;
      }

      for (let i = 0; i < snapshotsToDelete?.length; i++) {
        await props.deleteApi(snapshotsToDelete[i].metadata.name);
      }

      await queryClient.invalidateQueries({
        queryKey: SNAPSHOTS_QUERY_KEY(cacheKey, name),
      });

      Toast.success(t("core.snapshots.operations.cleanup.toast_success"));
    },
  });
}
</script>

<template>
  <VPageHeader :title="title">
    <template #icon>
      <IconHistoryLine />
    </template>
    <template #actions>
      <VButton size="sm" ghost @click="$router.back()">
        {{ $t("core.common.buttons.back") }}
      </VButton>
      <VButton size="sm" @click="handleToggleDiffMode">
        {{
          diffMode
            ? $t("core.snapshots.diff_mode.toggle.disable")
            : $t("core.snapshots.diff_mode.toggle.enable")
        }}
      </VButton>
      <VButton size="sm" type="danger" @click="handleCleanup">
        {{ $t("core.snapshots.operations.cleanup.button") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0">
        <div
          class="relative col-span-12 h-full overflow-auto sm:col-span-3 xl:col-span-2"
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
                  @click="handleSelectSnapshot(snapshot.metadata.name)"
                >
                  <SnapshotListItem
                    :snapshot="snapshot"
                    :cache-key="cacheKey"
                    :name="name"
                    :delete-api="deleteApi"
                    :revert-api="revertApi"
                    :release-snapshot="releaseSnapshot"
                    :base-snapshot="baseSnapshot"
                    :head-snapshot="headSnapshot"
                    :selected-snapshot-names="selectedSnapshotNames"
                  />
                </li>
              </ul>
            </Transition>
          </OverlayScrollbarsComponent>
        </div>
        <div
          class="col-span-12 h-full overflow-auto sm:col-span-9 xl:col-span-10"
        >
          <SnapshotContent
            v-if="!diffMode"
            :cache-key="cacheKey"
            :name="name"
            :get-api="getApi"
            :snapshot-names="selectedSnapshotNames"
          />
          <SnapshotDiffContent
            v-else
            :cache-key="cacheKey"
            :name="name"
            :get-api="getApi"
            :snapshot-names="selectedSnapshotNames"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
