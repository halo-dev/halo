<script lang="ts" setup>
import PostContributorList from "@/components/user/PostContributorList.vue";
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";

withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);
</script>

<template>
  <VEntityField>
    <template #description>
      <PostContributorList
        :owner="singlePage.owner"
        :contributors="singlePage.contributors"
      />
    </template>
  </VEntityField>
</template>
