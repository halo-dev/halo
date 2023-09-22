<script lang="ts" setup>
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import {
  IconNotification2Line,
  VCard,
  VLoading,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import { computed } from "vue";
import { useRouteQuery } from "@vueuse/router";
import NotificationListItem from "./components/NotificationListItem.vue";

const { currentUser } = useUserStore();

const activeTab = ref("unread");

const { data: notifications, isLoading } = useQuery({
  queryKey: ["user-notifications", activeTab],
  queryFn: async () => {
    const { data } = await apiClient.notification.listUserNotifications({
      username: currentUser?.metadata.name as string,
      unRead: activeTab.value === "unread",
    });

    return data;
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
  <VPageHeader title="消息">
    <template #icon>
      <IconNotification2Line class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VLoading v-if="isLoading" />
    <Transition v-else appear name="fade">
      <VCard
        style="height: calc(100vh - 5.5rem)"
        :body-class="['h-full', '!p-0']"
      >
        <div
          class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0"
        >
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
            </OverlayScrollbarsComponent>
          </div>
          <div class="col-span-12 sm:col-span-6 lg:col-span-7 xl:col-span-9">
            <iframe
              class="h-full w-full p-2"
              :srcdoc="selectedNotification?.spec?.htmlContent"
            ></iframe>
          </div>
        </div>
      </VCard>
    </Transition>
  </div>
</template>
