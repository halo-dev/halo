<script lang="ts" setup>
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";
import PostContributorList from "@/components/user/PostContributorList.vue";

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
