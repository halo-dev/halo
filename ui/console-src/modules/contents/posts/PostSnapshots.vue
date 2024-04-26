<script setup lang="ts">
import {
  IconHistoryLine,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRoute } from "vue-router";
import { apiClient } from "@/utils/api-client";
import { computed, watch } from "vue";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { useRouteQuery } from "@vueuse/router";
import SnapshotContent from "@console/modules/contents/posts/components/SnapshotContent.vue";
import SnapshotListItem from "@console/modules/contents/posts/components/SnapshotListItem.vue";

const route = useRoute();

const postName = computed(() => route.query.name as string);

const { data: post } = useQuery({
  queryKey: ["post-by-name", postName],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: postName.value,
      });
    return data;
  },
  enabled: computed(() => !!postName.value),
});

const { data: snapshots, isLoading } = useQuery({
  queryKey: ["post-snapshots-by-post-name", postName],
  queryFn: async () => {
    const { data } = await apiClient.post.listPostSnapshots({
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
</script>

<template>
  <VPageHeader :title="post?.spec.title">
    <template #icon>
      <IconHistoryLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton size="sm" @click="$router.back()">
        {{ $t("core.common.buttons.back") }}
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
                  @click="selectedSnapshotName = snapshot.metadata.name"
                >
                  <SnapshotListItem
                    :snapshot="snapshot"
                    :post="post"
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
            :post-name="postName"
            :snapshot-name="selectedSnapshotName"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
