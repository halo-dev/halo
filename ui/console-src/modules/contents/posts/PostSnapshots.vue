<script lang="ts" setup>
import BaseSnapshots from "@console/components/snapshots/BaseSnapshots.vue";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

const postName = computed(() => route.query.name as string);

const { data: post, isLoading } = useQuery({
  queryKey: ["core:post-by-name", postName],
  queryFn: async () => {
    const { data } = await coreApiClient.content.post.getPost({
      name: postName.value,
    });
    return data;
  },
  enabled: computed(() => !!postName.value),
});

async function listApi() {
  const { data } = await consoleApiClient.content.post.listPostSnapshots({
    name: postName.value,
  });
  return data;
}

async function deleteApi(snapshotName: string) {
  await consoleApiClient.content.post.deletePostContent({
    name: postName.value,
    snapshotName: snapshotName,
  });
}

async function revertApi(snapshotName: string) {
  await consoleApiClient.content.post.revertToSpecifiedSnapshotForPost({
    name: postName.value,
    revertSnapshotForPostParam: {
      snapshotName: snapshotName,
    },
  });
}

async function getApi(snapshotName: string) {
  const { data } = await consoleApiClient.content.post.fetchPostContent({
    name: postName.value,
    snapshotName: snapshotName,
  });
  return data;
}
</script>
<template>
  <VLoading v-if="isLoading" />
  <BaseSnapshots
    v-else
    :name="postName"
    cache-key="post"
    :title="post?.spec.title || ''"
    :release-snapshot="post?.spec.releaseSnapshot"
    :base-snapshot="post?.spec.baseSnapshot"
    :head-snapshot="post?.spec.headSnapshot"
    :list-api="listApi"
    :delete-api="deleteApi"
    :revert-api="revertApi"
    :get-api="getApi"
  />
</template>
