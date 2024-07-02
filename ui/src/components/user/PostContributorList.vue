<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { Contributor } from "@halo-dev/api-client";
import { VAvatar, VAvatarGroup } from "@halo-dev/components";
import { computed } from "vue";
import { useRouter } from "vue-router";

const props = withDefaults(
  defineProps<{
    allowViewUserDetail?: boolean;
    owner?: Contributor;
    contributors: Contributor[];
  }>(),
  {
    allowViewUserDetail: true,
    owner: undefined,
  }
);

const router = useRouter();
const { currentUserHasPermission } = usePermission();

const contributorsWithoutOwner = computed(() =>
  props.contributors.filter(
    (contributor) => contributor.name !== props.owner?.name
  )
);

function handleRouteToUserDetail(contributor: Contributor) {
  if (
    !currentUserHasPermission(["system:users:view"]) ||
    !props.allowViewUserDetail
  ) {
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
      v-if="owner"
      v-tooltip="owner?.displayName"
      :src="owner.avatar"
      :alt="owner.displayName"
      @click="handleRouteToUserDetail(owner)"
    />
    <VAvatar
      v-for="contributor in contributorsWithoutOwner"
      :key="contributor.name"
      v-tooltip="contributor.displayName"
      :src="contributor.avatar"
      :alt="contributor.displayName"
      @click="handleRouteToUserDetail(contributor)"
    ></VAvatar>
  </VAvatarGroup>
</template>
