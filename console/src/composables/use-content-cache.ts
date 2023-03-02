import { useLocalStorage } from "@vueuse/core";
import { Toast } from "@halo-dev/components";
import type { Ref } from "vue";

interface ContentCache {
  name: string;
  content?: string;
}
import debounce from "lodash.debounce";

interface useContentCacheReturn {
  handleResetCache: () => void;
  handleSetContentCache: () => void;
  handleClearCache: (name: string) => void;
}

export function useContentCache(
  key: string,
  name: string,
  raw: Ref<string | undefined>
): useContentCacheReturn {
  const content_caches = useLocalStorage<ContentCache[]>(key, []);

  const handleResetCache = () => {
    if (name) {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === name
      );
      if (cache) {
        Toast.info("已从缓存中恢复未保存的内容");
        raw.value = cache.content;
      }
    } else {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === "" && c.content
      );
      if (cache) {
        Toast.info("已从缓存中恢复未保存的内容");
        raw.value = cache.content;
      }
    }
  };

  const handleSetContentCache = debounce(() => {
    if (name) {
      const cache = content_caches.value.find(
        (c: ContentCache) => c.name === name
      );
      if (cache) {
        cache.content = raw?.value;
      } else {
        content_caches.value.push({
          name: name,
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
