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

    attachmentGroupSelect: {
      type: "attachmentGroupSelect";
      value?: string;
    };

    attachmentPolicySelect: {
      type: "attachmentPolicySelect";
      value?: string;
    };

    attachment: {
      type: "attachment";
      value?: string;
    };

    categoryCheckbox: {
      type: "categoryCheckbox";
      value?: string[];
    };

    tagSelect: {
      type: "tagSelect";
      value?: string | string[];
    };

    repeater: {
      type: "repeater";
      value?: Record<string, unknown>[];
    };

    categorySelect: {
      type: "categorySelect";
      value?: string | string[];
    };

    tagCheckbox: {
      type: "tagCheckbox";
      value?: string[];
    };

    singlePageSelect: {
      type: "singlePageSelect";
      value?: string;
    };

    roleSelect: {
      type: "roleSelect";
      value?: string;
    };

    postSelect: {
      type: "postSelect";
      value?: string;
    };

    menuRadio: {
      type: "menuRadio";
      value?: string;
    };

    menuItemSelect: {
      type: "menuItemSelect";
      value?: string;
    };

    menuCheckbox: {
      type: "menuCheckbox";
      value?: string[];
    };

    code: {
      type: "code";
      value?: string;
    };
  }
}
