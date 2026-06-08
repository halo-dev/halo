<script lang="ts" setup>
import type { Notification } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import { Dialog, Toast, VStatusDot } from "@halo-dev/components";
import { stores, utils } from "@halo-dev/ui-shared";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import sanitize from "sanitize-html";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    notification: Notification;
    isSelected: boolean;
    isSelectMode?: boolean;
    isChecked?: boolean;
  }>(),
  {
    isSelectMode: false,
    isChecked: false,
  }
);

const { currentUser } = stores.currentUser();

const isRead = ref();

const { mutate: handleMarkAsRead } = useMutation({
  mutationKey: ["notification-mark-as-read"],
  mutationFn: async ({ refetch }: { refetch: boolean }) => {
    const { data } =
      await ucApiClient.notification.notification.markNotificationAsRead({
        name: props.notification.metadata.name,
        username: currentUser?.user.metadata.name as string,
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
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    async onConfirm() {
      await ucApiClient.notification.notification.deleteSpecifiedNotification({
        name: props.notification.metadata.name,
        username: currentUser?.user.metadata.name as string,
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

const content = computed(() => {
  // Clean html tags
  return sanitize(props.notification.spec?.htmlContent || "", {
    allowedTags: [],
    allowedAttributes: {},
  });
});
</script>
<template>
  <div
    class="group relative flex cursor-pointer flex-col gap-2 p-4"
    :class="{ 'bg-gray-50': isSelected || isChecked }"
  >
    <div
      v-if="isSelected || isChecked"
      class="absolute inset-y-0 left-0 w-0.5 bg-primary"
    ></div>
    <div class="flex items-center gap-2">
      <!-- Checkbox shown in batch select mode -->
      <div
        v-if="isSelectMode"
        class="flex h-4 w-4 flex-none items-center justify-center rounded border transition-colors"
        :class="
          isChecked ? 'border-primary bg-primary' : 'border-gray-300 bg-white'
        "
      >
        <svg
          v-if="isChecked"
          class="h-3 w-3 text-white"
          viewBox="0 0 12 12"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M2 6l3 3 5-5"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>
      <div class="flex min-w-0 flex-1 items-center justify-between">
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
    </div>
    <div
      v-if="notification.spec?.htmlContent"
      class="line-clamp-1 text-xs text-gray-600"
    >
      {{ content }}
    </div>
    <div class="flex h-6 items-end justify-between">
      <div class="text-xs text-gray-600">
        {{ utils.date.timeAgo(notification.metadata.creationTimestamp) }}
      </div>
      <!-- Action buttons: hidden in batch select mode -->
      <div v-if="!isSelectMode" class="hidden space-x-2 group-hover:block">
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
