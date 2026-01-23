<script setup lang="ts">
import SnapshotContent from "@console/modules/contents/posts/components/SnapshotContent.vue";
import SnapshotListItem from "@console/modules/contents/posts/components/SnapshotListItem.vue";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
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
import { computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import SnapshotDiffContent from "./components/SnapshotDiffContent.vue";

const { t } = useI18n();
const route = useRoute();
const queryClient = useQueryClient();

const postName = computed(() => route.query.name as string);

const { data: post } = useQuery({
  queryKey: ["post-by-name", postName],
  queryFn: async () => {
    const { data } = await coreApiClient.content.post.getPost({
      name: postName.value,
    });
    return data;
  },
  enabled: computed(() => !!postName.value),
});

const { data: snapshots, isLoading } = useQuery({
  queryKey: ["post-snapshots-by-post-name", postName],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.post.listPostSnapshots({
      name: postName.value,
    });
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (item) => !!item.metadata.deletionTimestamp
    );
    return hasDeletingData ? 1000 : false;
  },
  enabled: computed(() => !!postName.value),
});

const selectedNames = useRouteQuery<string[] | undefined>("snapshot-names");
const diffModeQuery = useRouteQuery<string | undefined>("diff-mode");

const diffMode = computed(() => diffModeQuery.value === "true");

function handleToggleDiffMode() {
  if (diffMode.value) {
    selectedNames.value = [selectedNames.value?.[0]].filter(
      Boolean
    ) as string[];
  }
  diffModeQuery.value = !diffMode.value ? "true" : undefined;
}

function handleSelectSnapshot(snapshotName: string) {
  if (!diffMode.value) {
    selectedNames.value = [snapshotName];
    return;
  }

  // Diff mode
  if (selectedNames.value?.includes(snapshotName)) {
    selectedNames.value = selectedNames.value?.filter(
      (name) => name !== snapshotName
    );
    return;
  }

  if (selectedNames.value?.length === 2) {
    selectedNames.value = [snapshotName];
    return;
  }

  selectedNames.value = [...(selectedNames.value || []), snapshotName];

  selectedNames.value = selectedNames.value?.sort((a, b) => {
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

    if (!selectedNames.value?.length) {
      selectedNames.value = [value[0].metadata.name];
    }

    // Reset selectedSnapshotName if the selected snapshot is deleted
    if (
      !value.some((snapshot) =>
        selectedNames.value?.includes(snapshot.metadata.name)
      )
    ) {
      selectedNames.value = [value?.[0].metadata.name].filter(
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
    title: t("core.post_snapshots.operations.cleanup.title"),
    description: t("core.post_snapshots.operations.cleanup.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    async onConfirm() {
      const { releaseSnapshot, baseSnapshot, headSnapshot } =
        post.value?.spec || {};
      const snapshotsToDelete = snapshots.value?.filter((snapshot) => {
        const { name } = snapshot.metadata;
        return ![releaseSnapshot, baseSnapshot, headSnapshot]
          .filter(Boolean)
          .includes(name);
      });

      if (!snapshotsToDelete?.length) {
        Toast.info(t("core.post_snapshots.operations.cleanup.toast_empty"));
        return;
      }

      for (let i = 0; i < snapshotsToDelete?.length; i++) {
        await consoleApiClient.content.post.deletePostContent({
          name: postName.value,
          snapshotName: snapshotsToDelete[i].metadata.name,
        });
      }

      await queryClient.invalidateQueries({
        queryKey: ["post-snapshots-by-post-name", postName],
      });

      Toast.success(t("core.post_snapshots.operations.cleanup.toast_success"));
    },
  });
}
</script>

<template>
  <VPageHeader :title="post?.spec.title">
    <template #icon>
      <IconHistoryLine />
    </template>
    <template #actions>
      <VButton size="sm" ghost @click="$router.back()">
        {{ $t("core.common.buttons.back") }}
      </VButton>
      <VButton size="sm" @click="handleToggleDiffMode">
        {{ diffMode ? "关闭对比模式" : "开启对比模式" }}
      </VButton>
      <VButton size="sm" type="danger" @click="handleCleanup">
        {{ $t("core.post_snapshots.operations.cleanup.button") }}
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
                  @click="handleSelectSnapshot(snapshot.metadata.name)"
                >
                  <SnapshotListItem
                    :snapshot="snapshot"
                    :post="post"
                    :selected-names="selectedNames"
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
            v-if="!diffMode"
            :post-name="postName"
            :names="selectedNames"
          />
          <SnapshotDiffContent
            v-else
            :post-name="postName"
            :names="selectedNames"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
