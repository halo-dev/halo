import DefaultEditor from "@/components/editor/DefaultEditor.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import type { EditorProvider, PluginModule } from "@halo-dev/console-shared";
import { markRaw, onMounted, ref, type Ref } from "vue";

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
      component: markRaw(DefaultEditor),
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
