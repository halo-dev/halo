<script lang="ts" setup>
import PostContributorList from "@/components/user/PostContributorList.vue";
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
      <PostContributorList
        :owner="post.owner"
        :contributors="post.contributors"
      />
    </template>
  </VEntityField>
</template>
