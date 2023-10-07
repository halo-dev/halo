<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";
import type { DetailedUser } from "packages/api-client/dist";
import type { Ref } from "vue";
import { computed } from "vue";
import { inject } from "vue";

const user = inject<Ref<DetailedUser | undefined>>("user");

const { data } = useQuery({
  queryKey: ["notification-preferences"],
  queryFn: async () => {
    if (!user?.value) {
      return;
    }

    const { data } =
      await apiClient.notification.listUserNotificationPreferences({
        username: user?.value?.user.metadata.name,
      });

    return data;
  },
  enabled: computed(() => !!user?.value),
});
</script>

<template>
  <div>{{ data }}</div>
</template>
