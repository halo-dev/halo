<script lang="ts" setup>
import type { Info } from "@/types";
import { VAlert } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

const { data: info } = useQuery<Info>({
  queryKey: ["system-info"],
  queryFn: async () => {
    const { data } = await axios.get<Info>(`/actuator/info`, {
      withCredentials: true,
    });
    return data;
  },
  retry: 0,
});
</script>
<template>
  <VAlert
    v-if="info?.database.name.startsWith('H2')"
    class="mt-3"
    type="warning"
    :title="$t('core.components.h2_warning_alert.title')"
    :closable="false"
  >
    <template #description>
      {{ $t("core.components.h2_warning_alert.description") }}
    </template>
  </VAlert>
</template>
