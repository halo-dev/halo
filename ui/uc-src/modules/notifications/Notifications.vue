<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import {
  IconNotificationBadgeLine,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed } from "vue";
import { useRouteQuery } from "@vueuse/router";
import NotificationListItem from "./components/NotificationListItem.vue";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import NotificationContent from "./components/NotificationContent.vue";

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
    const { data } = await apiClient.notification.listUserNotifications({
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
</script>

<template>
  <VPageHeader :title="$t('core.uc_notification.title')">
    <template #icon>
      <IconNotificationBadgeLine class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0">
        <div
          class="relative col-span-12 h-full overflow-auto sm:col-span-6 lg:col-span-5 xl:col-span-3"
        >
          <OverlayScrollbarsComponent
            element="div"
            :options="{ scrollbars: { autoHide: 'scroll' } }"
            class="h-full w-full"
            defer
          >
            <VTabbar
              v-model:active-id="activeTab"
              class="sticky top-0 z-10 !rounded-none"
              :items="[
                { id: 'unread', label: $t('core.uc_notification.tabs.unread') },
                { id: 'read', label: $t('core.uc_notification.tabs.read') },
              ]"
              type="outline"
              @change="selectedNotificationName = undefined"
            ></VTabbar>
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
        <div class="col-span-12 sm:col-span-6 lg:col-span-7 xl:col-span-9">
          <NotificationContent :notification="selectedNotification" />
        </div>
      </div>
    </VCard>
  </div>
</template>
