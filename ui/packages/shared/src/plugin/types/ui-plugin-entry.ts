import type { PluginModule } from "./ui-plugin-module";

/**
 * Helper function to define a Halo UI plugin module with type safety.
 *
 * @param plugin - The plugin module definition containing components, routes, and extension points.
 * @returns The same plugin module, properly typed for Halo's plugin system.
 *
 * @remarks
 * This function is a type-safe wrapper that provides IDE support and type checking
 * for plugin definitions. It doesn't perform any runtime transformations, but ensures
 * that the plugin structure conforms to the expected interface.
 *
 * @example
 * ```typescript
 * import { definePlugin } from "@halo-dev/console-shared";
 * import { markRaw } from "vue";
 *
 * export default definePlugin({
 *   components: {
 *     "my-widget": MyWidgetComponent
 *   },
 *   routes: [
 *     {
 *       path: "/my-plugin",
 *       name: "MyPlugin",
 *       component: MyPluginView
 *     }
 *   ],
 *   extensionPoints: {
 *     "post:list-item:operation:create": (post) => [
 *       {
 *         priority: 10,
 *         component: markRaw(VButton),
 *         label: "Custom Action",
 *         action: () => {
 *           console.log("Action triggered");
 *         }
 *       }
 *     ]
 *   }
 * });
 * ```
 */
export function definePlugin(plugin: PluginModule): PluginModule {
  return plugin;
}
