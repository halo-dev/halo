/// <reference types="vite/client" />

export {};

import type { CoreMenuGroupId } from "@halo-dev/console-shared";

import "vue-router";

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  // eslint-disable-next-line
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

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
