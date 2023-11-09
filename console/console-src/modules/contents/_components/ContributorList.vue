<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { Contributor } from "@halo-dev/api-client";
import { VAvatar } from "@halo-dev/components";
import { useRouter } from "vue-router";

withDefaults(
  defineProps<{
    contributors: Contributor[];
  }>(),
  {}
);

const router = useRouter();
const { currentUserHasPermission } = usePermission();

function handleRouteToUserDetail(contributor: Contributor) {
  if (!currentUserHasPermission(["system:users:view"])) {
    return;
  }
  router.push({
    name: "UserDetail",
    params: { name: contributor.name },
  });
}
</script>

<template>
  <VAvatar
    v-for="(contributor, contributorIndex) in contributors"
    :key="contributorIndex"
    v-tooltip="contributor.displayName"
    size="xs"
    :src="contributor.avatar"
    :alt="contributor.displayName"
    circle
    @click="handleRouteToUserDetail(contributor)"
  ></VAvatar>
</template>
