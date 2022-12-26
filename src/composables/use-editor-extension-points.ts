import { usePluginModuleStore } from "@/stores/plugin";
import type { EditorProvider, PluginModule } from "@halo-dev/console-shared";
import { onMounted, ref, type Ref, defineAsyncComponent } from "vue";
import { VLoading } from "@halo-dev/components";

interface useEditorExtensionPointsReturn {
  editorProviders: Ref<EditorProvider[]>;
}

export function useEditorExtensionPoints(): useEditorExtensionPointsReturn {
  // resolve plugin extension points
  const { pluginModules } = usePluginModuleStore();

  const editorProviders = ref<EditorProvider[]>([
    {
      name: "default",
      displayName: "默认编辑器",
      component: defineAsyncComponent({
        loader: () => import("@/components/editor/DefaultEditor.vue"),
        loadingComponent: VLoading,
        delay: 200,
      }),
      rawType: "HTML",
    },
  ]);

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.["editor:create"]) {
        return;
      }

      const providers = extensionPoints["editor:create"]() as EditorProvider[];

      if (providers) {
        providers.forEach((provider) => {
          editorProviders.value.push(provider);
        });
      }
    });
  });

  return {
    editorProviders,
  };
}
