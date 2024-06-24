<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { Contributor } from "@halo-dev/api-client";
import { VAvatar, VAvatarGroup } from "@halo-dev/components";
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
  <VAvatarGroup size="xs" circle>
    <VAvatar
      v-for="contributor in contributors"
      :key="contributor.name"
      v-tooltip="contributor.displayName"
      :src="contributor.avatar"
      :alt="contributor.displayName"
      @click="handleRouteToUserDetail(contributor)"
    ></VAvatar>
  </VAvatarGroup>
</template>
