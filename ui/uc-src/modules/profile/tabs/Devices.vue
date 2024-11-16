<script lang="ts" setup>
import { ucApiClient } from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import DeviceListItem from "./components/DeviceListItem.vue";

const { data, isLoading } = useQuery({
  queryKey: ["uc:devices"],
  queryFn: async () => {
    const { data } = await ucApiClient.security.device.listDevices();
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (device) => !!device.device.metadata.deletionTimestamp
    );

    return hasDeletingData ? 1000 : false;
  },
});
</script>

<template>
  <VLoading v-if="isLoading" />

  <Transition v-else appear name="fade">
    <ul
      class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
      role="list"
    >
      <li v-for="device in data" :key="device?.device.spec.sessionId">
        <DeviceListItem :device="device" />
      </li>
    </ul>
  </Transition>
</template>
