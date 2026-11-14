import type { Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import { useQuery, type QueryClient } from "@tanstack/vue-query";
import { isAxiosError } from "axios";
import { computed, toValue, type MaybeRefOrGetter } from "vue";

export function useActivatedTheme(enabled: MaybeRefOrGetter<boolean> = true) {
  return useQuery({
    queryKey: ["activated-theme"],
    enabled: computed(
      () => toValue(enabled) && utils.permission.has(["system:themes:view"])
    ),
    staleTime: 30_000,
    retry: false,
    queryFn: async (): Promise<Theme | null> => {
      try {
        const { data } = await consoleApiClient.theme.theme.fetchActivatedTheme(
          { mute: true }
        );
        return data;
      } catch (error) {
        if (isAxiosError(error) && error.response?.status === 404) return null;
        throw error;
      }
    },
  });
}

// Theme mutations affect both the current theme and cached settings/search results.
export function invalidateThemeQueries(queryClient: QueryClient) {
  return Promise.all(
    [
      ["activated-theme"],
      ["installed-themes"],
      ["not-installed-themes"],
      ["themes"],
      ["theme-setting"],
      ["core:theme:configMap:data"],
      ["core", "global-search", "theme-settings"],
    ].map((queryKey) => queryClient.invalidateQueries({ queryKey }))
  );
}
