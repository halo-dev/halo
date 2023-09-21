<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import { relativeTimeTo } from "@/utils/date";
import type { Notification } from "@halo-dev/api-client";
import { VButton } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    notification: Notification;
    isSelected: boolean;
  }>(),
  {}
);

const { currentUser } = useUserStore();

const isRead = ref();

const { mutate: handleMarkAsRead } = useMutation({
  mutationKey: ["notification-mark-as-read"],
  mutationFn: async () => {
    apiClient.notification.markNotificationAsRead({
      name: props.notification.metadata.name,
      username: currentUser?.metadata.name as string,
    });
  },
  onSuccess() {
    isRead.value = true;
  },
});
</script>
<template>
  <div
    class="group relative flex cursor-pointer flex-col gap-2 p-4"
    :class="{ 'bg-gray-50': isSelected }"
  >
    <div
      v-if="isSelected"
      class="absolute inset-y-0 left-0 w-0.5 bg-primary"
    ></div>
    <div
      class="truncate text-sm"
      :class="{ 'font-semibold': notification.spec?.unread && !isRead }"
    >
      {{ notification.spec?.title }}
    </div>
    <div class="flex h-7 items-end justify-between">
      <div class="text-xs text-gray-600">
        {{ relativeTimeTo(notification.metadata.creationTimestamp) }}
      </div>
      <div class="hidden group-hover:block">
        <VButton
          v-if="notification.spec?.unread && !isRead"
          size="sm"
          @click="handleMarkAsRead"
        >
          标记为已读
        </VButton>
      </div>
    </div>
  </div>
</template>
