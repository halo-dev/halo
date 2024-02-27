<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import { relativeTimeTo } from "@/utils/date";
import {
  VButton,
  VCard,
  VEmpty,
  VEntity,
  VEntityField,
  VLoading,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import type { Notification } from "@halo-dev/api-client";

const { currentUser } = useUserStore();

const {
  data: notifications,
  isLoading,
  refetch,
  isFetching,
} = useQuery({
  queryKey: ["user-notifications"],
  queryFn: async () => {
    const { data } = await apiClient.notification.listUserNotifications({
      username: currentUser?.metadata.name as string,
      page: 1,
      size: 20,
      fieldSelector: ["spec.unread=true"],
    });

    return data.items;
  },
});

function handleRouteToNotification(notification: Notification) {
  window.location.href = `/uc/notifications?name=${notification.metadata.name}`;
}
</script>

<template>
  <VCard
    :body-class="['h-full', '@container', '!p-0', '!overflow-auto']"
    class="h-full"
    :title="$t('core.dashboard.widgets.presets.notification.title')"
  >
    <template #actions>
      <div style="padding: 12px 16px">
        <a
          class="text-sm text-gray-600 hover:text-gray-900"
          href="/uc/notifications"
        >
          {{ $t("core.common.buttons.view_all") }}
        </a>
      </div>
    </template>
    <VLoading v-if="isLoading" />
    <VEmpty
      v-else-if="!notifications?.length"
      :title="$t('core.dashboard.widgets.presets.notification.empty.title')"
    >
      <template #actions>
        <VButton :loading="isFetching" @click="refetch">
          {{ $t("core.common.buttons.refresh") }}
        </VButton>
      </template>
    </VEmpty>
    <OverlayScrollbarsComponent
      v-else
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li
          v-for="notification in notifications"
          :key="notification.metadata.name"
        >
          <VEntity>
            <template #start>
              <VEntityField
                :title="notification.spec?.title"
                :description="notification.spec?.rawContent"
                @click="handleRouteToNotification(notification)"
              />
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <span class="truncate text-xs tabular-nums text-gray-500">
                    {{
                      relativeTimeTo(notification.metadata.creationTimestamp)
                    }}
                  </span>
                </template>
              </VEntityField>
            </template>
          </VEntity>
        </li>
      </ul>
    </OverlayScrollbarsComponent>
  </VCard>
</template>
