import { useLocalStorage } from "@vueuse/core";
import { Toast } from "@halo-dev/components";
import type { Ref } from "vue";

interface ContentCache {
  name: string;
  content?: string;
}
import debounce from "lodash.debounce";
import { useI18n } from "vue-i18n";

interface useContentCacheReturn {
  handleResetCache: () => void;
  handleSetContentCache: () => void;
  handleClearCache: (name: string) => void;
}

export function useContentCache(
  key: string,
  name: Ref<string | undefined>,
  raw: Ref<string | undefined>
): useContentCacheReturn {
  const content_caches = useLocalStorage<ContentCache[]>(key, []);
  const { t } = useI18n();

  const handleResetCache = () => {
    if (name.value) {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === name.value
      );
      if (cache) {
        Toast.info(t("core.composables.content_cache.toast_recovered"));
        raw.value = cache.content;
      }
    } else {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === "" && c.content
      );
      if (cache) {
        Toast.info(t("core.composables.content_cache.toast_recovered"));
        raw.value = cache.content;
      }
    }
  };

  const handleSetContentCache = debounce(() => {
    if (name.value) {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === name.value
      );
      if (cache) {
        cache.content = raw?.value;
      } else {
        content_caches.value.push({
          name: name.value || "",
          content: raw?.value,
        });
      }
    } else {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === ""
      );
      if (cache) {
        cache.content = raw?.value;
      } else {
        content_caches.value.push({
          name: "",
          content: raw?.value,
        });
      }
    }
  }, 500);

  const handleClearCache = (name: string) => {
    if (name) {
      const index = content_caches.value.findIndex(
        (c: ContentCache) => c.name === name
      );
      index > -1 && content_caches.value.splice(index, 1);
    } else {
      const index = content_caches.value.findIndex(
        (c: ContentCache) => c.name === ""
      );
      index > -1 && content_caches.value.splice(index, 1);
    }
  };

  return {
    handleClearCache,
    handleResetCache,
    handleSetContentCache,
  };
}
