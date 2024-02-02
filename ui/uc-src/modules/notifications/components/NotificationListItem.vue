<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import { relativeTimeTo } from "@/utils/date";
import type { Notification } from "@halo-dev/api-client";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { Dialog, Toast, VStatusDot } from "@halo-dev/components";
import { watch } from "vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

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
  mutationFn: async ({ refetch }: { refetch: boolean }) => {
    const { data } = await apiClient.notification.markNotificationAsRead({
      name: props.notification.metadata.name,
      username: currentUser?.metadata.name as string,
    });

    if (refetch) {
      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
    }

    return data;
  },
  onSuccess() {
    isRead.value = true;
  },
});

function handleDelete() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete.title"),
    description: t("core.uc_notification.operations.delete.description"),
    async onConfirm() {
      await apiClient.notification.deleteSpecifiedNotification({
        name: props.notification.metadata.name,
        username: currentUser?.metadata.name as string,
      });

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

watch(
  () => props.isSelected,
  (value) => {
    if (value && props.notification.spec?.unread) {
      handleMarkAsRead({ refetch: false });
    }
  },
  {
    immediate: true,
  }
);
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
    <div class="flex items-center justify-between">
      <div
        class="truncate text-sm"
        :class="{ 'font-semibold': notification.spec?.unread && !isRead }"
      >
        {{ notification.spec?.title }}
      </div>
      <VStatusDot
        v-if="notification.metadata.deletionTimestamp"
        v-tooltip="$t('core.common.status.deleting')"
        state="warning"
        animate
      />
    </div>
    <div
      v-if="notification.spec?.rawContent"
      class="truncate text-xs text-gray-600"
    >
      {{ notification.spec.rawContent }}
    </div>
    <div class="flex h-6 items-end justify-between">
      <div class="text-xs text-gray-600">
        {{ relativeTimeTo(notification.metadata.creationTimestamp) }}
      </div>
      <div class="hidden space-x-2 group-hover:block">
        <span
          v-if="notification.spec?.unread && !isRead"
          class="text-sm text-gray-600 hover:text-gray-900"
          @click.stop="handleMarkAsRead({ refetch: true })"
        >
          {{ $t("core.uc_notification.operations.mark_as_read.button") }}
        </span>
        <span
          class="text-sm text-red-600 hover:text-red-700"
          @click.stop="handleDelete"
        >
          {{ $t("core.common.buttons.delete") }}
        </span>
      </div>
    </div>
  </div>
</template>
