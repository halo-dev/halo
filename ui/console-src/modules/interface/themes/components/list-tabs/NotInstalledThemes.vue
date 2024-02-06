<script lang="ts" setup>
import { VButton, VEmpty, VSpace, VLoading } from "@halo-dev/components";
import ThemeListItem from "../ThemeListItem.vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";

const {
  data: themes,
  isLoading,
  isFetching,
  refetch,
} = useQuery<Theme[]>({
  queryKey: ["not-installed-themes"],
  queryFn: async () => {
    const { data } = await apiClient.theme.listThemes({
      page: 0,
      size: 0,
      uninstalled: true,
    });
    return data.items;
  },
});
</script>

<template>
  <div id="not-installed-themes-wrapper">
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!themes?.length" appear name="fade">
      <VEmpty :title="$t('core.theme.list_modal.not_installed_empty.title')">
        <template #actions>
          <VSpace>
            <VButton :loading="isFetching" @click="refetch">
              {{ $t("core.common.buttons.refresh") }}
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <ul class="box-border h-full w-full space-y-3" role="list">
        <li v-for="(theme, index) in themes" :key="index">
          <ThemeListItem :theme="theme" :installed="false" />
        </li>
      </ul>
    </Transition>
  </div>
</template>
