import { useThemeStore } from "@console/stores/theme";
import type {
  Theme,
  ThemeV1alpha1ConsoleApiListThemesRequest,
} from "@halo-dev/api-client";
import { consoleApiClient, paginate } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { computed, toValue, type MaybeRefOrGetter } from "vue";

export function useInstalledThemes(enabled: MaybeRefOrGetter<boolean> = true) {
  const themeStore = useThemeStore();
  return useQuery<Theme[]>({
    enabled: computed(() => toValue(enabled)),
    queryKey: ["installed-themes"],
    queryFn: async () => {
      const themes = await paginate<
        ThemeV1alpha1ConsoleApiListThemesRequest,
        Theme
      >((params) => consoleApiClient.theme.theme.listThemes(params), {
        uninstalled: false,
        size: 1000,
      });

      return themes.sort((a, b) => {
        const activatedThemeName = themeStore.activatedTheme?.metadata.name;
        if (a.metadata.name === activatedThemeName) {
          return -1;
        }
        if (b.metadata.name === activatedThemeName) {
          return 1;
        }
        return 0;
      });
    },
    refetchInterval(data) {
      const hasDeletingTheme = data?.some(
        (theme) => !!theme.metadata.deletionTimestamp
      );

      return hasDeletingTheme ? 1000 : false;
    },
  });
}
