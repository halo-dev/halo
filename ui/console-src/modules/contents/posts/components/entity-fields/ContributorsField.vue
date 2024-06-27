<script lang="ts" setup>
import ContributorList from "@console/modules/contents/_components/ContributorList.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";

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
      <ContributorList :contributors="post.contributors" />
    </template>
  </VEntityField>
</template>
