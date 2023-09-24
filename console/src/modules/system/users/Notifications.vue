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
      unRead: activeTab.value === "unread",
    });

    return data;
  },
  cacheTime: 0,
});

const selectedNotificationName = useRouteQuery<string | undefined>("name");

const selectedNotification = computed(() => {
  return notifications.value?.items.find(
    (item) => item.metadata.name === selectedNotificationName.value
  );
});
</script>

<template>
  <VPageHeader title="消息">
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
                { id: 'unread', label: '未读' },
                { id: 'read', label: '已读' },
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
                    ? '当前没有未读的消息'
                    : '当前没有已读的消息'
                }`"
              >
                <template #actions>
                  <VButton :loading="isFetching && !isLoading" @click="refetch">
                    刷新
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
