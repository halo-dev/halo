<script lang="ts" setup>
import { VAvatar, VEntityField } from "@halo-dev/components";
import type { ListedPost } from "@halo-dev/api-client";

withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);
</script>

<template>
  <VEntityField>
    <template #description>
      <RouterLink
        v-for="(contributor, contributorIndex) in post.contributors"
        :key="contributorIndex"
        :to="{
          name: 'UserDetail',
          params: { name: contributor.name },
        }"
        class="flex items-center"
      >
        <VAvatar
          v-tooltip="contributor.displayName"
          size="xs"
          :src="contributor.avatar"
          :alt="contributor.displayName"
          circle
        ></VAvatar>
      </RouterLink>
    </template>
  </VEntityField>
</template>
