<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { relativeTimeTo } from "@/utils/date";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { Notification } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import {
  VButton,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import sanitize from "sanitize-html";

const { currentUser } = useUserStore();

const {
  data: notifications,
  isLoading,
  refetch,
  isFetching,
} = useQuery({
  queryKey: ["user-notifications"],
  queryFn: async () => {
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
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
  <WidgetCard
    :body-class="['@container', '!overflow-auto']"
    :title="$t('core.dashboard.widgets.presets.notification.title')"
  >
    <template #actions>
      <a
        class="text-sm text-gray-600 hover:text-gray-900"
        href="/uc/notifications"
      >
        {{ $t("core.common.buttons.view_all") }}
      </a>
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
      <VEntityContainer>
        <VEntity
          v-for="notification in notifications"
          :key="notification.metadata.name"
        >
          <template #start>
            <VEntityField
              :title="notification.spec?.title"
              :description="
                sanitize(notification.spec?.htmlContent || '', {
                  allowedTags: [],
                  allowedAttributes: {},
                })
              "
              @click="handleRouteToNotification(notification)"
            />
          </template>
          <template #end>
            <VEntityField>
              <template #description>
                <span class="truncate text-xs tabular-nums text-gray-500">
                  {{ relativeTimeTo(notification.metadata.creationTimestamp) }}
                </span>
              </template>
            </VEntityField>
          </template>
        </VEntity>
      </VEntityContainer>
    </OverlayScrollbarsComponent>
  </WidgetCard>
</template>
