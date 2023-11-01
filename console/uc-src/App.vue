<script lang="ts" setup>
import { useQuery } from "@tanstack/vue-query";

const { data, isLoading } = useQuery({
  queryKey: ["user"],
  queryFn: async () => {
    const response = await fetch(`/apis/api.console.halo.run/v1alpha1/users/-`);
    const data = await response.json();
    return data;
  },
});
</script>

<template>
  <div v-if="isLoading">Loading...</div>
  <div v-else>
    Hi {{ data.user.spec.displayName }}, here is user center page.
  </div>
</template>
