import type { PluginModule, RouteRecordAppend } from "@halo-dev/ui-shared";
import type { RouteRecordRaw } from "vue-router";

export interface RouteDeclaration {
  name?: string;
  path?: string;
  parentName?: string;
}

/**
 * Flattens declared routes without registering or executing anything. Append
 * declarations keep their parent route name; nested children are listed with
 * their own path and name.
 */
export function collectRouteDeclarations(
  routes?: RouteRecordRaw[] | RouteRecordAppend[]
): RouteDeclaration[] {
  if (!routes) {
    return [];
  }
  const declarations: RouteDeclaration[] = [];
  for (const item of routes) {
    const isAppend = "parentName" in item;
    const route = isAppend ? item.route : item;
    declarations.push({
      name: route.name ? String(route.name) : undefined,
      path: route.path,
      parentName: isAppend ? String(item.parentName) : undefined,
    });
    if (route.children?.length) {
      declarations.push(...collectRouteDeclarations(route.children));
    }
  }
  return declarations;
}

export interface UiModuleSummary {
  consoleRoutes: RouteDeclaration[];
  ucRoutes: RouteDeclaration[];
  components: string[];
  extensionPoints: string[];
  formkitInputs: string[];
}

/**
 * Lists the declarations of an already-registered module. Extension point and
 * FormKit callbacks are never invoked; only their keys are read.
 */
export function summarizePluginModule(module?: PluginModule): UiModuleSummary {
  return {
    consoleRoutes: collectRouteDeclarations(module?.routes),
    ucRoutes: collectRouteDeclarations(module?.ucRoutes),
    components: Object.keys(module?.components || {}),
    extensionPoints: Object.keys(module?.extensionPoints || {}),
    formkitInputs: Object.keys(module?.formkit?.inputs || {}),
  };
}

export function hasDeclaredCapabilities(summary: UiModuleSummary): boolean {
  return (
    summary.consoleRoutes.length > 0 ||
    summary.ucRoutes.length > 0 ||
    summary.components.length > 0 ||
    summary.extensionPoints.length > 0 ||
    summary.formkitInputs.length > 0
  );
}
