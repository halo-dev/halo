import { storeToRefs } from "pinia";
import { useGlobalInfoStore } from "@/stores/global-info";
import { useRoute } from "vue-router";
import { useTitle } from "@vueuse/core";
import { computed, type Ref } from "vue";
import { AppName } from "@/constants/app";
import { useI18n } from "vue-i18n";

export function useAppTitle(baseTitle?: Ref<string>) {
  const { globalInfo } = storeToRefs(useGlobalInfoStore());

  const { t } = useI18n();
  const route = useRoute();

  useTitle(
    computed(() => {
      const { title: routeTitle } = route.meta;
      const siteTitle = globalInfo.value?.siteTitle || AppName;
      return [t(baseTitle?.value || routeTitle || ""), siteTitle]
        .filter(Boolean)
        .join(" - ");
    })
  );
}
