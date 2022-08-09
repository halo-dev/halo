import type { Component, Ref } from "vue";
import type { RouteRecordRaw, RouteRecordName } from "vue-router";
import type { MenuGroupType } from "./menus";
import type { PagesPublicState } from "../states/pages";

export type ExtensionPointName = "PAGES" | "POSTS";

export type ExtensionPointState = PagesPublicState;

interface RouteRecordAppend {
  parentName: RouteRecordName;
  route: RouteRecordRaw;
}

export interface Plugin {
  name: string;

  /**
   * These components will be registered when plugin is activated.
   */
  components?: Component[];

  /**
   * Activate hook will be called when plugin is activated.
   */
  activated?: () => void;

  /**
   * Deactivate hook will be called when plugin is deactivated.
   */
  deactivated?: () => void;

  routes?: RouteRecordRaw[] | RouteRecordAppend[];

  menus?: MenuGroupType[];

  extensionPoints?: {
    [key in ExtensionPointName]?: (state: Ref<ExtensionPointState>) => void;
  };
}
