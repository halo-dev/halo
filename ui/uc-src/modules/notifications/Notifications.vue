<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { ucApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconCheckboxCircle,
  IconDeleteBin,
  IconNotificationBadgeLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "lodash-es";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import NotificationContent from "./components/NotificationContent.vue";
import NotificationListItem from "./components/NotificationListItem.vue";

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = useUserStore();

const activeTab = useRouteQuery("tab", "unread");

const {
  data: notifications,
  isLoading,
  refetch,
  isFetching,
} = useQuery({
  queryKey: ["user-notifications", activeTab],
  queryFn: async () => {
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.metadata.name as string,
        fieldSelector: [`spec.unread=${activeTab.value === "unread"}`],
      });

    return data;
  },
  cacheTime: 0,
  refetchInterval(data) {
    const hasDeletingNotifications = data?.items.some(
      (item) => item.metadata.deletionTimestamp !== undefined
    );

    return hasDeletingNotifications ? 1000 : false;
  },
});

const selectedNotificationName = useRouteQuery<string | undefined>("name");

const selectedNotification = computed(() => {
  return notifications.value?.items.find(
    (item) => item.metadata.name === selectedNotificationName.value
  );
});

function handleDeleteNotifications() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_all.title"),
    description: t("core.uc_notification.operations.delete_all.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (!notifications.value || notifications.value.items.length === 0) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const notificationChunks = chunk(notifications.value.items, 5);

      for (const chunk of notificationChunks) {
        await Promise.all(
          chunk.map((notification) =>
            ucApiClient.notification.notification.deleteSpecifiedNotification({
              username: currentUser.metadata.name,
              name: notification.metadata.name,
            })
          )
        );
      }

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

function handleMarkAllAsRead() {
  Dialog.warning({
    title: t("core.uc_notification.operations.mark_all_as_read.title"),
    description: t(
      "core.uc_notification.operations.mark_all_as_read.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      if (!notifications.value || notifications.value.items.length === 0) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const names = notifications.value?.items.map(
        (notification) => notification.metadata.name
      );

      await ucApiClient.notification.notification.markNotificationsAsRead({
        username: currentUser.metadata.name,
        markSpecifiedRequest: {
          names,
        },
      });

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
    },
  });
}
</script>

<template>
  <VPageHeader :title="$t('core.uc_notification.title')">
    <template #icon>
      <IconNotificationBadgeLine />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0">
        <div
          class="relative col-span-12 flex h-full flex-col overflow-hidden sm:col-span-6 lg:col-span-5 xl:col-span-3"
        >
          <div class="sticky top-0 z-10 flex-none">
            <VTabbar
              v-model:active-id="activeTab"
              class="!rounded-none"
              :items="[
                { id: 'unread', label: $t('core.uc_notification.tabs.unread') },
                { id: 'read', label: $t('core.uc_notification.tabs.read') },
              ]"
              type="outline"
              @change="selectedNotificationName = undefined"
            ></VTabbar>

            <div
              class="absolute right-4 top-1/2 flex -translate-y-1/2 items-center gap-2"
            >
              <button
                v-if="activeTab === 'unread'"
                class="flex h-7 w-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                :disabled="!notifications?.items.length"
                @click="handleMarkAllAsRead"
              >
                <IconCheckboxCircle
                  class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                />
              </button>
              <button
                class="group flex h-7 w-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                :disabled="!notifications?.items.length"
                @click="handleDeleteNotifications"
              >
                <IconDeleteBin
                  class="h-4 w-4 text-gray-600 group-hover:text-red-600"
                />
              </button>
            </div>
          </div>
          <OverlayScrollbarsComponent
            element="div"
            :options="{ scrollbars: { autoHide: 'scroll' } }"
            class="h-full w-full flex-1"
            defer
          >
            <VLoading v-if="isLoading" />
            <Transition
              v-else-if="!notifications?.items.length"
              appear
              name="fade"
            >
              <VEmpty
                :title="`${
                  activeTab === 'unread'
                    ? $t('core.uc_notification.empty.titles.unread')
                    : $t('core.uc_notification.empty.titles.read')
                }`"
              >
                <template #actions>
                  <VButton :loading="isFetching && !isLoading" @click="refetch">
                    {{ $t("core.common.buttons.refresh") }}
                  </VButton>
                </template>
              </VEmpty>
            </Transition>
            <Transition v-else appear name="fade">
              <ul
                class="box-border h-full w-full divide-y divide-gray-100"
                role="list"
              >
                <li
                  v-for="notification in notifications?.items"
                  :key="notification.metadata.name"
                  @click="selectedNotificationName = notification.metadata.name"
                >
                  <NotificationListItem
                    :notification="notification"
                    :is-selected="
                      selectedNotificationName === notification.metadata.name
                    "
                  />
                </li>
              </ul>
            </Transition>
          </OverlayScrollbarsComponent>
        </div>
        <div
          class="col-span-12 overflow-auto sm:col-span-6 lg:col-span-7 xl:col-span-9"
        >
          <NotificationContent :notification="selectedNotification" />
        </div>
      </div>
    </VCard>
  </div>
</template>
