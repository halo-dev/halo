import { useActivatedTheme } from "@console/composables/use-activated-theme";
import type {
  Theme,
  ThemeV1alpha1ConsoleApiListThemesRequest,
} from "@halo-dev/api-client";
import { consoleApiClient, paginate } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { computed, toValue, type MaybeRefOrGetter } from "vue";

export function useInstalledThemes(enabled: MaybeRefOrGetter<boolean> = true) {
  const { data: activatedTheme } = useActivatedTheme(enabled);
  const query = useQuery<Theme[]>({
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

      return themes;
    },
    refetchInterval(data) {
      const hasDeletingTheme = data?.some(
        (theme) => !!theme.metadata.deletionTimestamp
      );

      return hasDeletingTheme ? 1000 : false;
    },
  });
  return {
    ...query,
    data: computed(() =>
      query.data.value
        ?.slice()
        .sort(
          (a, b) =>
            Number(b.metadata.name === activatedTheme.value?.metadata.name) -
            Number(a.metadata.name === activatedTheme.value?.metadata.name)
        )
    ),
  };
}
