import type { Component } from "vue";
import type { RouteRecordRaw } from "vue-router";
import type { MenuGroupType } from "./menus";

export type ExtensionPointName =
  | "POSTS"
  | "POST_EDITOR"
  | "DASHBOARD"
  | "USER_SETTINGS";

// TODO define extension point state
export type ExtensionPointState = Record<string, unknown>;

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

  routes?: RouteRecordRaw[];

  menus?: MenuGroupType[];

  extensionPoints?: Record<
    ExtensionPointName,
    (state: ExtensionPointState) => void
  >;
}
