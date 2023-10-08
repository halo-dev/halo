<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import type { DetailedUser } from "@halo-dev/api-client";
import { VLoading, VSwitch } from "@halo-dev/components";
import type { Ref } from "vue";
import { computed } from "vue";
import { inject } from "vue";
import cloneDeep from "lodash.clonedeep";
import type { ReasonTypeNotifierRequest } from "@halo-dev/api-client";

const queryClient = useQueryClient();

const user = inject<Ref<DetailedUser | undefined>>("user");

const { data, isLoading } = useQuery({
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

const {
  mutate,
  isLoading: mutating,
  variables,
} = useMutation({
  mutationKey: ["update-notification-preferences"],
  mutationFn: async ({
    state,
    reasonTypeIndex,
    notifierIndex,
  }: {
    state: boolean;
    reasonTypeIndex: number;
    notifierIndex: number;
  }) => {
    const preferences = cloneDeep(data.value);

    if (!user?.value || !preferences) {
      return;
    }

    if (!preferences.stateMatrix) {
      preferences.stateMatrix = [];
    }

    preferences.stateMatrix[reasonTypeIndex][notifierIndex] = state;

    const reasonTypeNotifiers = data.value?.reasonTypes
      ?.map((reasonType, currentReasonTypeIndex) => {
        return {
          reasonType: reasonType.name,
          notifiers: data.value?.notifiers
            ?.map((notifier, currentNotifierIndex) => {
              if (
                preferences.stateMatrix?.[currentReasonTypeIndex][
                  currentNotifierIndex
                ]
              ) {
                return notifier.name;
              }
            })
            .filter(Boolean),
        };
      })
      .filter(Boolean) as Array<ReasonTypeNotifierRequest>;

    return await apiClient.notification.saveUserNotificationPreferences({
      username: user?.value?.user.metadata.name,
      reasonTypeNotifierCollectionRequest: {
        reasonTypeNotifiers,
      },
    });
  },
  onSuccess() {
    queryClient.invalidateQueries({ queryKey: ["notification-preferences"] });
  },
});
</script>

<template>
  <VLoading v-if="isLoading" />

  <Transition v-else appear name="fade">
    <div class="box-border h-full w-full overflow-auto rounded-base border">
      <table class="min-w-full divide-y divide-gray-100">
        <thead class="bg-gray-50">
          <tr>
            <th
              class="px-4 py-3 text-left text-sm font-semibold text-gray-900 sm:w-96"
              scope="col"
            >
              {{ $t("core.user.notification-preferences.fields.type") }}
            </th>
            <th
              v-for="notifier in data?.notifiers"
              :key="notifier.name"
              scope="col"
              class="px-4 py-3 text-left text-sm font-semibold text-gray-900"
            >
              {{ notifier.displayName }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100 bg-white">
          <tr
            v-for="(reasonType, index) in data?.reasonTypes"
            :key="reasonType.name"
          >
            <td
              class="whitespace-nowrap px-4 py-3 text-sm font-medium text-gray-900"
            >
              {{ reasonType.displayName }}
            </td>
            <td
              v-for="(notifier, notifierIndex) in data?.notifiers"
              :key="notifier.name"
              class="whitespace-nowrap px-4 py-3 text-sm text-gray-500"
            >
              <VSwitch
                :model-value="data?.stateMatrix?.[index][notifierIndex]"
                :loading="
                  mutating &&
                  variables?.reasonTypeIndex === index &&
                  variables?.notifierIndex === notifierIndex
                "
                @change="
                  mutate({
                    state: !data?.stateMatrix?.[index][notifierIndex],
                    reasonTypeIndex: index,
                    notifierIndex: notifierIndex,
                  })
                "
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </Transition>
</template>
