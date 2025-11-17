/// <reference types="vite/client" />

export {};

import type { Component } from "vue";
import type { CoreMenuGroupId } from "./src/types/menus";

declare module "vue-router" {
  interface RouteMeta {
    title?: string;
    searchable?: boolean;
    permissions?: string[];
    core?: boolean;
    menu?: {
      name: string;
      group?: CoreMenuGroupId;
      icon?: Component;
      priority: number;
      mobile?: boolean;
    };
  }
}
