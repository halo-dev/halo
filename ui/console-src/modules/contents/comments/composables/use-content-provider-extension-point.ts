import { usePluginModuleStore } from "@/stores/plugin";
import type { CommentContentProvider } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { markRaw } from "vue";
import DefaultCommentContent from "../components/DefaultCommentContent.vue";

export function useContentProviderExtensionPoint() {
  const defaultProvider: CommentContentProvider = {
    component: markRaw(DefaultCommentContent),
  };

  const { pluginModules } = usePluginModuleStore();

  return useQuery({
    queryKey: ["core:comment:list-item:content:provider"],
    queryFn: async () => {
      const result: CommentContentProvider[] = [];
      for (const pluginModule of pluginModules) {
        const callbackFunction =
          pluginModule?.extensionPoints?.["comment:list-item:content:replace"];

        if (typeof callbackFunction !== "function") {
          continue;
        }

        const item = await callbackFunction();

        result.push(item);
      }

      if (result.length) {
        return result[0];
      }

      return defaultProvider;
    },
  });
}
