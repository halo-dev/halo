<script lang="ts" setup>
import BaseSnapshots from "@console/components/snapshots/BaseSnapshots.vue";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

const singlePageName = computed(() => route.query.name as string);

const { data: singlePage, isLoading } = useQuery({
  queryKey: ["core:singlePage-by-name", singlePageName],
  queryFn: async () => {
    const { data } = await coreApiClient.content.singlePage.getSinglePage({
      name: singlePageName.value,
    });
    return data;
  },
  enabled: computed(() => !!singlePageName.value),
});

async function listApi() {
  const { data } =
    await consoleApiClient.content.singlePage.listSinglePageSnapshots({
      name: singlePageName.value,
    });
  return data;
}

async function deleteApi(snapshotName: string) {
  await consoleApiClient.content.singlePage.deleteSinglePageContent({
    name: singlePageName.value,
    snapshotName: snapshotName,
  });
}

async function revertApi(snapshotName: string) {
  await consoleApiClient.content.singlePage.revertToSpecifiedSnapshotForSinglePage(
    {
      name: singlePageName.value,
      revertSnapshotForSingleParam: {
        snapshotName: snapshotName,
      },
    }
  );
}

async function getApi(snapshotName: string) {
  const { data } =
    await consoleApiClient.content.singlePage.fetchSinglePageContent({
      name: singlePageName.value,
      snapshotName: snapshotName,
    });
  return data;
}
</script>
<template>
  <VLoading v-if="isLoading" />
  <BaseSnapshots
    v-else
    :name="singlePageName"
    cache-key="singlePage"
    :title="singlePage?.spec.title || ''"
    :release-snapshot="singlePage?.spec.releaseSnapshot"
    :base-snapshot="singlePage?.spec.baseSnapshot"
    :head-snapshot="singlePage?.spec.headSnapshot"
    :list-api="listApi"
    :delete-api="deleteApi"
    :revert-api="revertApi"
    :get-api="getApi"
  />
</template>
