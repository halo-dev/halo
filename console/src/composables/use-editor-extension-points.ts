import { usePluginModuleStore } from "@/stores/plugin";
import type { EditorProvider, PluginModule } from "@halo-dev/console-shared";
import { onMounted, ref, type Ref, defineAsyncComponent } from "vue";
import { VLoading } from "@halo-dev/components";
import Logo from "@/assets/logo.png";
import { useI18n } from "vue-i18n";

interface useEditorExtensionPointsReturn {
  editorProviders: Ref<EditorProvider[]>;
}

export function useEditorExtensionPoints(): useEditorExtensionPointsReturn {
  // resolve plugin extension points
  const { pluginModules } = usePluginModuleStore();
  const { t } = useI18n();

  const editorProviders = ref<EditorProvider[]>([
    {
      name: "default",
      displayName: t("core.plugin.extension_points.editor.providers.default"),
      component: defineAsyncComponent({
        loader: () => import("@/components/editor/DefaultEditor.vue"),
        loadingComponent: VLoading,
        delay: 200,
      }),
      rawType: "HTML",
      logo: Logo,
    },
  ]);

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.["editor:create"]) {
        return;
      }

      const providers = extensionPoints["editor:create"]() as EditorProvider[];

      editorProviders.value.push(...providers);
    });
  });

  return {
    editorProviders,
  };
}
