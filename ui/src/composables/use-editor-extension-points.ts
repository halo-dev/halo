import Logo from "@/assets/logo.png";
import { usePluginModuleStore } from "@/stores/plugin";
import { VLoading } from "@halo-dev/components";
import type { EditorProvider } from "@halo-dev/console-shared";
import { defineAsyncComponent, markRaw, ref, type Ref } from "vue";
import { useI18n } from "vue-i18n";

interface useEditorExtensionPointsReturn {
  editorProviders: Ref<EditorProvider[]>;
  fetchEditorProviders: () => Promise<void>;
}

export function useEditorExtensionPoints(): useEditorExtensionPointsReturn {
  // resolve plugin extension points
  const { pluginModules } = usePluginModuleStore();
  const { t } = useI18n();

  const editorProviders = ref<EditorProvider[]>([
    {
      name: "default",
      displayName: t("core.plugin.extension_points.editor.providers.default"),
      component: markRaw(
        defineAsyncComponent({
          loader: () => import("@/components/editor/DefaultEditor.vue"),
          loadingComponent: VLoading,
          delay: 200,
        })
      ),
      rawType: "HTML",
      logo: Logo,
    },
  ]);

  async function fetchEditorProviders() {
    for (const pluginModule of pluginModules) {
      try {
        const callbackFunction =
          pluginModule?.extensionPoints?.["editor:create"];

        if (typeof callbackFunction !== "function") {
          continue;
        }

        const pluginProviders = await callbackFunction();

        editorProviders.value.push(...pluginProviders);
      } catch (error) {
        console.error(`Error processing plugin module:`, pluginModule, error);
      }
    }
  }

  return {
    editorProviders,
    fetchEditorProviders,
  };
}
