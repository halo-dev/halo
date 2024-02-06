<script lang="ts" setup>
import { VEntityField } from "@halo-dev/components";
import type { ListedPost } from "@halo-dev/api-client";
import ContributorList from "@console/modules/contents/_components/ContributorList.vue";

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
