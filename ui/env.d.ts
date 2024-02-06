/// <reference types="vite/client" />
/// <reference types="unplugin-icons/types/vue" />

export {};

import type { CoreMenuGroupId } from "@halo-dev/console-shared";
import type { FormKitInputs } from "@formkit/inputs";

import "vue-router";

import "axios";

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  // eslint-disable-next-line
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module "vue-router" {
  import type { Component } from "vue";

  interface RouteMeta {
    title?: string;
    description?: string;
    searchable?: boolean;
    permissions?: string[];
    core?: boolean;
    hideFooter?: boolean;
    menu?: {
      name: string;
      group?: CoreMenuGroupId;
      icon?: Component;
      priority: number;
      mobile?: boolean;
    };
  }
}

declare module "axios" {
  export interface AxiosRequestConfig {
    mute?: boolean;
  }
}

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    "datetime-local": {
      type: "datetime-local";
      value?: string;
    };
  }
}
