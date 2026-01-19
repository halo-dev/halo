<script lang="ts" setup>
import { paginate } from "@/utils/paginate";
import type {
  Theme,
  ThemeV1alpha1ConsoleApiListThemesRequest,
} from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { VButton, VEmpty, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import ThemeListItem from "../ThemeListItem.vue";

const {
  data: themes,
  isLoading,
  isFetching,
  refetch,
} = useQuery<Theme[]>({
  queryKey: ["not-installed-themes"],
  queryFn: async () => {
    return await paginate<ThemeV1alpha1ConsoleApiListThemesRequest, Theme>(
      (params) => consoleApiClient.theme.theme.listThemes(params),
      {
        uninstalled: true,
        size: 1000,
      }
    );
  },
});
</script>

<template>
  <div id="not-installed-themes-wrapper">
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!themes?.length" appear name="fade">
      <VEmpty :title="$t('core.theme.list_modal.not_installed_empty.title')">
        <template #actions>
          <VButton :loading="isFetching" @click="refetch">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
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
